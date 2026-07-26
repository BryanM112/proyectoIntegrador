package ec.edu.ups.icc.proyectointegrador.security.ratelimit;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;

@Service
public class RateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterService.class);

    private final StringRedisTemplate redisTemplate;

    public RateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

        public RateLimitResult tryConsume(
            String key,
            int maxRequests,
            Duration window
        ) {
        try {
            Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1L) {
            redisTemplate.expire(key, window);
        }

        long ttlSeconds = getTtl(key);

        if (ttlSeconds <= 0) {
            ttlSeconds = window.toSeconds();
        }

        long currentCount = count != null ? count : 0;
        boolean allowed = count != null && count <= maxRequests;

        return new RateLimitResult(
                allowed,
                ttlSeconds,
                currentCount
        );

        } catch (RedisConnectionFailureException ex) {
            log.error(
                "No se pudo conectar con Redis para aplicar rate limiting. "
                        + "La solicitud será permitida.",
                ex
        );

        return new RateLimitResult(
                true,
                0,
                0
        );
     }
    }

    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (RedisConnectionFailureException ex) {
            log.error("No se pudo consultar Redis. Se asumirá que la clave no existe.", ex);
            return false;
        }
    }

    public void block(String key, Duration duration) {
        try {
            redisTemplate.opsForValue().set(key, "1", duration);
        } catch (RedisConnectionFailureException ex) {
            log.error("No se pudo crear el bloqueo en Redis.", ex);
        }
    }

    public long getTtl(String key) {
        try {
            Long ttl = redisTemplate.getExpire(key);
            return ttl != null && ttl > 0 ? ttl : 0;
        } catch (RedisConnectionFailureException ex) {
            log.error("No se pudo consultar el TTL en Redis.", ex);
            return 0;
        }
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (RedisConnectionFailureException ex) {
            log.error("No se pudo eliminar la clave de Redis.", ex);
        }
    }
}