package ec.edu.ups.icc.proyectointegrador.security.dtos;

public record AuthResponseDto(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {
}