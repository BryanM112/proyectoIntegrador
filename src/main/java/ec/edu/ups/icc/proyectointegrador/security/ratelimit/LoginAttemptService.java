package ec.edu.ups.icc.proyectointegrador.security.ratelimit;

import java.time.Duration;

import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration ATTEMPTS_WINDOW = Duration.ofMinutes(15);
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(15);

    private final RateLimiterService rateLimiterService;

    public LoginAttemptService(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    public boolean isBlocked(String ip, String email) {
        return rateLimiterService.exists(blockedUserKey(email))
                || rateLimiterService.exists(blockedIpKey(ip));
    }

    public void registerFailedAttempt(String ip, String email) {
        RateLimitResult userResult = rateLimiterService.tryConsume(
                failedAttemptsUserKey(email), MAX_FAILED_ATTEMPTS, ATTEMPTS_WINDOW);

        if (userResult.getCurrentCount() >= MAX_FAILED_ATTEMPTS) {
            rateLimiterService.block(blockedUserKey(email), BLOCK_DURATION);
        }

        RateLimitResult ipResult = rateLimiterService.tryConsume(
                failedAttemptsIpKey(ip), MAX_FAILED_ATTEMPTS, ATTEMPTS_WINDOW);

        if (ipResult.getCurrentCount() >= MAX_FAILED_ATTEMPTS) {
            rateLimiterService.block(blockedIpKey(ip), BLOCK_DURATION);
        }
    }

    public long getBlockRetryAfterSeconds(String ip, String email) {
        return Math.max(
                rateLimiterService.getTtl(blockedUserKey(email)),
                rateLimiterService.getTtl(blockedIpKey(ip))
        );
    }

    public void resetFailedAttempts(String ip, String email) {
        rateLimiterService.delete(failedAttemptsUserKey(email));
        rateLimiterService.delete(failedAttemptsIpKey(ip));
    }

    private String failedAttemptsUserKey(String email) { return "failed-attempts-user:" + email; }
    private String failedAttemptsIpKey(String ip) { return "failed-attempts-ip:" + ip; }
    private String blockedUserKey(String email) { return "blocked-user:" + email; }
    private String blockedIpKey(String ip) { return "blocked-ip:" + ip; }
}