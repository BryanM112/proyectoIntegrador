package ec.edu.ups.icc.proyectointegrador.security.ratelimit;

public class RateLimitResult {
    private final boolean allowed;
    private final long retryAfterSeconds;

    public RateLimitResult(boolean allowed, long retryAfterSeconds) {
        this.allowed = allowed;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public boolean isAllowed() { return allowed; }
    public long getRetryAfterSeconds() { return retryAfterSeconds; }
}