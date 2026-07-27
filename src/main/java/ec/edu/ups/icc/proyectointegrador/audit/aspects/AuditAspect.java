package ec.edu.ups.icc.proyectointegrador.audit.aspects;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ec.edu.ups.icc.proyectointegrador.audit.enums.AuditResult;
import ec.edu.ups.icc.proyectointegrador.audit.services.AuditService;
import ec.edu.ups.icc.proyectointegrador.users.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;

/*
 * Registra automáticamente en audit_logs cada operación de escritura
 * (POST, PUT, PATCH, DELETE) que pasa por un @RestController, sin que
 * cada módulo tenga que invocar nada manualmente.
 */
@Aspect
@Component
public class AuditAspect {

    private static final java.util.Set<String> SENSITIVE_FIELDS = java.util.Set.of(
            "password", "passwordhash", "token", "accesstoken", "refreshtoken"
    );

    private final AuditService auditService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AuditAspect(AuditService auditService, UserRepository userRepository) {
        this.auditService = auditService;
        this.userRepository = userRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Around("within(@org.springframework.web.bind.annotation.RestController *) "
            + "&& (@annotation(org.springframework.web.bind.annotation.PostMapping) "
            + "|| @annotation(org.springframework.web.bind.annotation.PutMapping) "
            + "|| @annotation(org.springframework.web.bind.annotation.PatchMapping) "
            + "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping))")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {

        HttpServletRequest request = currentRequest();
        String httpMethod = request != null ? request.getMethod() : "UNKNOWN";
        String endpoint = request != null ? request.getRequestURI() : "unknown";
        String ipAddress = request != null ? extractClientIp(request) : "unknown";
        String correlationId = UUID.randomUUID().toString();

        String resourceType = resolveResourceType(joinPoint);
        String action = httpMethod + "_" + resourceType;
        Long actorId = resolveActorId();
        String newValue = resolveNewValue(joinPoint);

        try {
            Object result = joinPoint.proceed();

            Long resourceId = resolveResourceId(joinPoint, result);
            auditService.record(actorId, action, resourceType, resourceId, newValue,
                    AuditResult.SUCCESS, ipAddress, httpMethod, endpoint, correlationId);

            return result;
        } catch (Throwable ex) {
            Long resourceId = resolveResourceIdFromArgs(joinPoint);
            auditService.record(actorId, action, resourceType, resourceId, newValue,
                    AuditResult.FAILED, ipAddress, httpMethod, endpoint, correlationId);
            throw ex;
        }
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String resolveResourceType(ProceedingJoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String withoutSuffix = className.replace("Controller", "");
        return withoutSuffix.toUpperCase(Locale.ROOT);
    }

    private Long resolveActorId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        return userRepository.findWithRolesByEmail(authentication.getName())
                .map(user -> user.getId())
                .orElse(null);
    }

    private String resolveNewValue(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        java.lang.annotation.Annotation[][] paramAnnotations = method.getParameterAnnotations();

        for (int i = 0; i < paramAnnotations.length; i++) {
            for (java.lang.annotation.Annotation annotation : paramAnnotations[i]) {
                if (annotation instanceof RequestBody && args[i] != null) {
                    return scrubAndSerialize(args[i]);
                }
            }
        }
        return null;
    }

    private String scrubAndSerialize(Object body) {
        try {
            java.util.Map<String, Object> asMap = objectMapper.convertValue(body, java.util.Map.class);
            java.util.Map<String, Object> scrubbed = new java.util.LinkedHashMap<>();

            asMap.forEach((key, value) -> {
                if (SENSITIVE_FIELDS.contains(key.toLowerCase(Locale.ROOT))) {
                    scrubbed.put(key, "***");
                } else {
                    scrubbed.put(key, value);
                }
            });

            return objectMapper.writeValueAsString(scrubbed);
        } catch (Exception ex) {
            return null;
        }
    }

    private Long resolveResourceId(ProceedingJoinPoint joinPoint, Object result) {
        Long fromArgs = resolveResourceIdFromArgs(joinPoint);
        if (fromArgs != null) {
            return fromArgs;
        }
        return resolveResourceIdFromResult(result);
    }

    private Long resolveResourceIdFromArgs(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        java.lang.annotation.Annotation[][] paramAnnotations = method.getParameterAnnotations();

        for (int i = 0; i < paramAnnotations.length; i++) {
            for (java.lang.annotation.Annotation annotation : paramAnnotations[i]) {
                if (annotation instanceof PathVariable && args[i] instanceof Long value) {
                    return value;
                }
            }
        }
        return null;
    }

    private Long resolveResourceIdFromResult(Object result) {
        if (result == null) {
            return null;
        }
        try {
            Method getId = result.getClass().getMethod("getId");
            Object id = getId.invoke(result);
            return id instanceof Long value ? value : null;
        } catch (Exception ex) {
            return null;
        }
    }
}