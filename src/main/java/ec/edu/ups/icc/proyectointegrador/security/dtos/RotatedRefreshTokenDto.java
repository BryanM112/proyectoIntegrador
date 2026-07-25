package ec.edu.ups.icc.proyectointegrador.security.dtos;

import ec.edu.ups.icc.proyectointegrador.users.entities.UserEntity;

public record RotatedRefreshTokenDto(
        UserEntity user,
        CreatedRefreshTokenDto refreshToken
) {
}