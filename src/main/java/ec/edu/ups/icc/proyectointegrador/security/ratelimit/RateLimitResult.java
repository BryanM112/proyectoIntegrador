package ec.edu.ups.icc.proyectointegrador.security.ratelimit;

public class RateLimitResult {

    private final boolean allowed;
    private final long retryAfterSeconds;
    private final long currentCount;

    public RateLimitResult(
            boolean allowed,
            long retryAfterSeconds,
            long currentCount
    ) {
        this.allowed = allowed;
        this.retryAfterSeconds = retryAfterSeconds;
        this.currentCount = currentCount;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    public long getCurrentCount() {
        return currentCount;
    }
}