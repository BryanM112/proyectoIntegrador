package ec.edu.ups.icc.proyectointegrador.audit.aspects;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ec.edu.ups.icc.proyectointegrador.audit.enums.AuditResult;
import ec.edu.ups.icc.proyectointegrador.audit.services.AuditService;
import ec.edu.ups.icc.proyectointegrador.users.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuditAspect {

    private static final Logger logger =
            LoggerFactory.getLogger(AuditAspect.class);

    private static final String CORRELATION_ID_KEY =
            "correlationId";

    private static final Set<String> SENSITIVE_FIELDS =
            Set.of(
                    "password",
                    "passwordhash",
                    "currentpassword",
                    "newpassword",
                    "confirmpassword",
                    "token",
                    "accesstoken",
                    "refreshtoken",
                    "authorization",
                    "secret",
                    "jwtsecret"
            );

    private final AuditService auditService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AuditAspect(
            AuditService auditService,
            UserRepository userRepository,
            ObjectMapper objectMapper
    ) {
        this.auditService = auditService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Around("""
            @within(org.springframework.web.bind.annotation.RestController)
            && (
                @annotation(org.springframework.web.bind.annotation.PostMapping)
                || @annotation(org.springframework.web.bind.annotation.PutMapping)
                || @annotation(org.springframework.web.bind.annotation.PatchMapping)
                || @annotation(org.springframework.web.bind.annotation.DeleteMapping)
            )
            """)
    public Object audit(
            ProceedingJoinPoint joinPoint
    ) throws Throwable {

        HttpServletRequest request = currentRequest();

        String httpMethod = request != null
                ? request.getMethod()
                : "UNKNOWN";

        String endpoint = request != null
                ? request.getRequestURI()
                : "unknown";

        String ipAddress = request != null
                ? extractClientIp(request)
                : "unknown";

        String correlationId =
                UUID.randomUUID().toString();

        String previousCorrelationId =
                MDC.get(CORRELATION_ID_KEY);

        MDC.put(
                CORRELATION_ID_KEY,
                correlationId
        );

        String resourceType =
                resolveResourceType(joinPoint);

        String action =
                httpMethod + "_" + resourceType;

        Long actorId =
                resolveActorIdSafely(correlationId);

        String newValue =
                resolveNewValue(joinPoint);

        try {
            Object result = joinPoint.proceed();

            Long resourceId =
                    resolveResourceId(
                            joinPoint,
                            result
                    );

            recordSafely(
                    actorId,
                    action,
                    resourceType,
                    resourceId,
                    newValue,
                    AuditResult.SUCCESS,
                    ipAddress,
                    httpMethod,
                    endpoint,
                    correlationId
            );

            return result;

        } catch (Throwable originalException) {
            Long resourceId =
                    resolveResourceIdFromArgs(
                            joinPoint
                    );

            recordSafely(
                    actorId,
                    action,
                    resourceType,
                    resourceId,
                    newValue,
                    AuditResult.FAILED,
                    ipAddress,
                    httpMethod,
                    endpoint,
                    correlationId
            );

            throw originalException;

        } finally {
            restoreCorrelationId(
                    previousCorrelationId
            );
        }
    }

    private HttpServletRequest currentRequest() {
        Object attributes =
                RequestContextHolder.getRequestAttributes();

        if (attributes
                instanceof ServletRequestAttributes servletAttributes) {

            return servletAttributes.getRequest();
        }

        return null;
    }

    private String extractClientIp(
            HttpServletRequest request
    ) {
        /*
         * No se confía directamente en X-Forwarded-For,
         * porque cualquier cliente podría falsificarlo.
         */
        return request.getRemoteAddr();
    }

    private String resolveResourceType(
            ProceedingJoinPoint joinPoint
    ) {
        String className = joinPoint
                .getSignature()
                .getDeclaringType()
                .getSimpleName();

        String withoutSuffix =
                className.replaceFirst(
                        "Controller$",
                        ""
                );

        return withoutSuffix.toUpperCase(
                Locale.ROOT
        );
    }

    private Long resolveActorIdSafely(
            String correlationId
    ) {
        try {
            Authentication authentication =
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication();

            if (authentication == null
                    || !authentication.isAuthenticated()
                    || "anonymousUser".equals(
                            authentication.getPrincipal()
                    )) {

                return null;
            }

            return userRepository
                    .findWithRolesByEmail(
                            authentication.getName()
                    )
                    .map(user -> user.getId())
                    .orElse(null);

        } catch (Exception ex) {
            logger.error(
                    "No se pudo determinar el actor de la auditoría. correlationId={}",
                    correlationId,
                    ex
            );

            return null;
        }
    }

    private String resolveNewValue(
            ProceedingJoinPoint joinPoint
    ) {
        MethodSignature signature =
                (MethodSignature) joinPoint.getSignature();

        Method method = signature.getMethod();
        Object[] arguments = joinPoint.getArgs();

        Annotation[][] parameterAnnotations =
                method.getParameterAnnotations();

        for (int index = 0;
                index < parameterAnnotations.length;
                index++) {

            for (Annotation annotation
                    : parameterAnnotations[index]) {

                if (annotation instanceof RequestBody
                        && arguments[index] != null) {

                    return scrubAndSerialize(
                            arguments[index]
                    );
                }
            }
        }

        return null;
    }

    private String scrubAndSerialize(
            Object body
    ) {
        try {
            JsonNode root =
                    objectMapper.valueToTree(body);

            scrubNode(root);

            return objectMapper
                    .writeValueAsString(root);

        } catch (Exception ex) {
            logger.warn(
                    "No se pudo serializar el cuerpo para auditoría",
                    ex
            );

            return null;
        }
    }

    private void scrubNode(
            JsonNode node
    ) {
        if (node == null) {
            return;
        }

        if (node.isObject()) {
            ObjectNode objectNode =
                    (ObjectNode) node;

            Iterator<Map.Entry<String, JsonNode>>
                    fields = objectNode.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field =
                        fields.next();

                String normalizedName =
                        normalizeFieldName(
                                field.getKey()
                        );

                if (SENSITIVE_FIELDS.contains(
                        normalizedName
                )) {
                    objectNode.put(
                            field.getKey(),
                            "***"
                    );
                } else {
                    scrubNode(
                            field.getValue()
                    );
                }
            }

            return;
        }

        if (node.isArray()) {
            node.forEach(this::scrubNode);
        }
    }

    private String normalizeFieldName(
            String fieldName
    ) {
        return fieldName
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
    }

    private Long resolveResourceId(
            ProceedingJoinPoint joinPoint,
            Object result
    ) {
        Long idFromArguments =
                resolveResourceIdFromArgs(
                        joinPoint
                );

        if (idFromArguments != null) {
            return idFromArguments;
        }

        return resolveResourceIdFromResult(
                result
        );
    }

    private Long resolveResourceIdFromArgs(
            ProceedingJoinPoint joinPoint
    ) {
        MethodSignature signature =
                (MethodSignature) joinPoint.getSignature();

        Method method = signature.getMethod();
        Object[] arguments = joinPoint.getArgs();

        Annotation[][] parameterAnnotations =
                method.getParameterAnnotations();

        for (int index = 0;
                index < parameterAnnotations.length;
                index++) {

            for (Annotation annotation
                    : parameterAnnotations[index]) {

                if (annotation instanceof PathVariable
                        && arguments[index]
                        instanceof Long value) {

                    return value;
                }
            }
        }

        return null;
    }

    private Long resolveResourceIdFromResult(
            Object result
    ) {
        if (result == null) {
            return null;
        }

        Object responseBody = result;

        if (result
                instanceof ResponseEntity<?> responseEntity) {

            responseBody =
                    responseEntity.getBody();
        }

        if (responseBody == null) {
            return null;
        }

        Long idFromRecord =
                invokeIdMethod(
                        responseBody,
                        "id"
                );

        if (idFromRecord != null) {
            return idFromRecord;
        }

        return invokeIdMethod(
                responseBody,
                "getId"
        );
    }

    private Long invokeIdMethod(
            Object target,
            String methodName
    ) {
        try {
            Method method = target
                    .getClass()
                    .getMethod(methodName);

            Object value =
                    method.invoke(target);

            return value instanceof Long id
                    ? id
                    : null;

        } catch (ReflectiveOperationException ex) {
            return null;
        }
    }

    private void recordSafely(
            Long actorId,
            String action,
            String resourceType,
            Long resourceId,
            String newValue,
            AuditResult result,
            String ipAddress,
            String httpMethod,
            String endpoint,
            String correlationId
    ) {
        try {
            auditService.record(
                    actorId,
                    action,
                    resourceType,
                    resourceId,
                    newValue,
                    result,
                    ipAddress,
                    httpMethod,
                    endpoint,
                    correlationId
            );

        } catch (Exception auditException) {
            logger.error(
                    "La auditoría falló, pero la operación principal continuará. correlationId={}",
                    correlationId,
                    auditException
            );
        }
    }

    private void restoreCorrelationId(
            String previousCorrelationId
    ) {
        if (previousCorrelationId == null) {
            MDC.remove(CORRELATION_ID_KEY);
        } else {
            MDC.put(
                    CORRELATION_ID_KEY,
                    previousCorrelationId
            );
        }
    }
}