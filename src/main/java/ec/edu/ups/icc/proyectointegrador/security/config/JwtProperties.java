package ec.edu.ups.icc.proyectointegrador.security.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        String secret,
        Duration accessExpiration,
        Duration refreshExpiration
) {
}
