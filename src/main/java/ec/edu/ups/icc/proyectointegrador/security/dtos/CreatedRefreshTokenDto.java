package ec.edu.ups.icc.proyectointegrador.security.dtos;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreatedRefreshTokenDto(
        String token,
        UUID tokenId,
        OffsetDateTime expiresAt
) {
}