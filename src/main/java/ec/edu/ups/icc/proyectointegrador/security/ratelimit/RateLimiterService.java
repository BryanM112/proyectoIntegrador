package ec.edu.ups.icc.proyectointegrador.security.ratelimit;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;

    public RateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public RateLimitResult tryConsume(String key, int maxRequests, Duration window) {
        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1L) {
            redisTemplate.expire(key, window);
        }

        long ttlSeconds = getTtl(key);
        if (ttlSeconds <= 0) {
            ttlSeconds = window.toSeconds();
        }

        boolean allowed = count != null && count <= maxRequests;

        return new RateLimitResult(allowed, ttlSeconds);
    }

    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void block(String key, Duration duration) {
        redisTemplate.opsForValue().set(key, "1", duration);
    }

    public long getTtl(String key) {
        Long ttl = redisTemplate.getExpire(key);
        return ttl != null && ttl > 0 ? ttl : 0;
    }
}