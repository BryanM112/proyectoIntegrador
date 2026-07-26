package ec.edu.ups.icc.proyectointegrador.security.ratelimit;

import java.io.IOException;
import java.time.Duration;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ec.edu.ups.icc.proyectointegrador.core.dtos.ErrorResponseDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int LOGIN_LIMIT = 5;
    private static final Duration LOGIN_WINDOW = Duration.ofMinutes(1);

    private static final int REGISTER_LIMIT = 3;
    private static final Duration REGISTER_WINDOW = Duration.ofHours(1);

    private static final int PUBLIC_LIMIT = 60;
    private static final Duration PUBLIC_WINDOW = Duration.ofMinutes(1);

    private static final int AUTHENTICATED_LIMIT = 120;
    private static final Duration AUTHENTICATED_WINDOW = Duration.ofMinutes(1);

    private static final int REPORTS_LIMIT = 5;
    private static final Duration REPORTS_WINDOW = Duration.ofMinutes(1);

    private final RateLimiterService rateLimiterService;
    private final LoginAttemptService loginAttemptService;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(
            RateLimiterService rateLimiterService,
            LoginAttemptService loginAttemptService
    ) {
        this.rateLimiterService = rateLimiterService;
        this.loginAttemptService = loginAttemptService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        String ip = extractClientIp(request);

        if (path.endsWith("/auth/login")) {
            handleLogin(request, response, filterChain, ip);
            return;
        }

        if (path.endsWith("/auth/register")) {
            applyLimit(response, filterChain, request, "rate:register:" + ip, REGISTER_LIMIT, REGISTER_WINDOW);
            return;
        }

        if (isReportsEndpoint(path)) {
            applyLimit(response, filterChain, request, "rate:reports:" + currentUsername(ip), REPORTS_LIMIT, REPORTS_WINDOW);
            return;
        }

        if (isAuthenticated()) {
            applyLimit(response, filterChain, request, "rate:auth:" + currentUsername(ip), AUTHENTICATED_LIMIT, AUTHENTICATED_WINDOW);
            return;
        }

        applyLimit(response, filterChain, request, "rate:public:" + ip, PUBLIC_LIMIT, PUBLIC_WINDOW);
    }

    private void handleLogin(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain,
            String ip
    ) throws ServletException, IOException {

        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);

        String email = extractEmail(cachedRequest);

        if (loginAttemptService.isBlocked(ip, email)) {
            writeTooManyRequests(response, loginAttemptService.getBlockRetryAfterSeconds(ip, email), request.getRequestURI());
            return;
        }

        RateLimitResult result = rateLimiterService.tryConsume("rate:login:" + ip + ":" + email, LOGIN_LIMIT, LOGIN_WINDOW);

        if (!result.isAllowed()) {
            writeTooManyRequests(response, result.getRetryAfterSeconds(), request.getRequestURI());
            return;
        }

        filterChain.doFilter(cachedRequest, response);

        // Cualquier respuesta distinta de 200 en login se considera un intento fallido
        if (response.getStatus() != HttpServletResponse.SC_OK) {
            loginAttemptService.registerFailedAttempt(ip, email);
        }
    }

    private void applyLimit(
            HttpServletResponse response,
            FilterChain filterChain,
            HttpServletRequest request,
            String key,
            int limit,
            Duration window
    ) throws ServletException, IOException {

        RateLimitResult result = rateLimiterService.tryConsume(key, limit, window);

        if (!result.isAllowed()) {
            writeTooManyRequests(response, result.getRetryAfterSeconds(), request.getRequestURI());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isReportsEndpoint(String path) {
        return path.contains("/reports/") || path.matches(".*/registrations/[^/]+/certificate$");
    }

    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    private String currentUsername(String fallbackIp) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication != null && authentication.getName() != null) ? authentication.getName() : fallbackIp;
    }

    private String extractEmail(CachedBodyHttpServletRequest request) {
        try {
            byte[] body = request.getCachedBody();
            if (body.length == 0) {
                return "unknown";
            }
            JsonNode node = objectMapper.readTree(body);
            JsonNode emailNode = node.get("email");
            return emailNode != null ? emailNode.asText().trim().toLowerCase() : "unknown";
        } catch (IOException ex) {
            return "unknown";
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void writeTooManyRequests(HttpServletResponse response, long retryAfterSeconds, String path) throws IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponseDto body = new ErrorResponseDto(429, "TOO_MANY_REQUESTS",
                "Ha superado el límite de solicitudes permitidas. Intente nuevamente más tarde.", path, null);

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}