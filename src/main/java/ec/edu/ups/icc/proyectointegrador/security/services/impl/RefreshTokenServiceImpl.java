package ec.edu.ups.icc.proyectointegrador.security.services.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

import ec.edu.ups.icc.proyectointegrador.core.exceptions.InvalidRefreshTokenException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.ups.icc.proyectointegrador.security.config.JwtProperties;
import ec.edu.ups.icc.proyectointegrador.security.dtos.CreatedRefreshTokenDto;
import ec.edu.ups.icc.proyectointegrador.core.exceptions.InternalServerException;
import ec.edu.ups.icc.proyectointegrador.security.dtos.RotatedRefreshTokenDto;
import ec.edu.ups.icc.proyectointegrador.security.entities.RefreshTokenEntity;
import ec.edu.ups.icc.proyectointegrador.security.repositories.RefreshTokenRepository;
import ec.edu.ups.icc.proyectointegrador.security.services.RefreshTokenService;
import ec.edu.ups.icc.proyectointegrador.users.entities.UserEntity;
import ec.edu.ups.icc.proyectointegrador.users.enums.UserStatus;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final int TOKEN_BYTES = 64;

    private final RefreshTokenRepository repository;
    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom;

    public RefreshTokenServiceImpl(
            RefreshTokenRepository repository,
            JwtProperties jwtProperties
    ) {
        this.repository = repository;
        this.jwtProperties = jwtProperties;
        this.secureRandom = new SecureRandom();
    }

    @Override
    @Transactional
    public CreatedRefreshTokenDto create(
            UserEntity user,
            String clientIp
    ) {
        String rawToken = generateSecureToken();
        String tokenHash = hash(rawToken);

        OffsetDateTime createdAt =
                OffsetDateTime.now(ZoneOffset.UTC);

        OffsetDateTime expiresAt = createdAt.plus(
                jwtProperties.refreshExpiration()
        );

        UUID tokenId = UUID.randomUUID();

        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setTokenId(tokenId);
        entity.setUser(user);
        entity.setTokenHash(tokenHash);
        entity.setExpiresAt(expiresAt);
        entity.setRevokedAt(null);
        entity.setCreatedAt(createdAt);
        entity.setCreatedByIp(normalizeIp(clientIp));
        entity.setReplacedByTokenId(null);

        repository.save(entity);

        return new CreatedRefreshTokenDto(
                rawToken,
                tokenId,
                expiresAt
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshTokenEntity validate(String rawToken) {
        String tokenHash = hash(rawToken);

        RefreshTokenEntity token = repository
                .findByTokenHash(tokenHash)
                .orElseThrow(() ->
                        new InvalidRefreshTokenException(
                                "Refresh token inválido"
                        )
                );

        OffsetDateTime now =
                OffsetDateTime.now(ZoneOffset.UTC);

        if (token.getRevokedAt() != null) {
            throw new InvalidRefreshTokenException(
                    "Refresh token revocado"
            );
        }

        if (!token.getExpiresAt().isAfter(now)) {
            throw new InvalidRefreshTokenException(
                    "Refresh token expirado"
            );
        }

        if (token.getUser().getStatus() != UserStatus.ACTIVE) {
            throw new InvalidRefreshTokenException(
                    "Usuario no disponible"
            );
        }

        return token;
    }

    @Override
    @Transactional
    public String hash(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException(
                    "El refresh token es obligatorio"
            );
        }

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hashBytes);

        } catch (NoSuchAlgorithmException exception) {
            throw new InternalServerException(
                    "No se pudo procesar el refresh token",
                    exception
            );
        }
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[TOKEN_BYTES];

        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    private String normalizeIp(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            return null;
        }

        String normalized = clientIp.trim();

        if (normalized.length() > 45) {
            return normalized.substring(0, 45);
        }

        return normalized;
    }

    @Override
    @Transactional
    public RotatedRefreshTokenDto rotate(String rawToken, String clientIp) {
        RefreshTokenEntity currentToken = validate(rawToken);
        CreatedRefreshTokenDto newToken = create(
            currentToken.getUser(),
            clientIp
        );

        OffsetDateTime revokedAt = OffsetDateTime.now(ZoneOffset.UTC);

        currentToken.setRevokedAt(revokedAt);
        currentToken.setReplacedByTokenId(newToken.tokenId());
        repository.save(currentToken);

        return new RotatedRefreshTokenDto(
            currentToken.getUser(),
            newToken
        );
    }

    @Override
    public void revoke(String rawToken) {

        String tokenHash = hash(rawToken);

        RefreshTokenEntity token = repository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new InvalidRefreshTokenException(
                            "Refresh token inválido"));

        if (token.getRevokedAt() != null) {
            return;
        }

        token.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));

        repository.save(token);
    }
}