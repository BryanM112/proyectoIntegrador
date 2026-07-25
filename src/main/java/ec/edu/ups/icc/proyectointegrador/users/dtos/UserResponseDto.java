package ec.edu.ups.icc.proyectointegrador.users.dtos;

import java.time.OffsetDateTime;
import java.util.Set;

import ec.edu.ups.icc.proyectointegrador.roles.enums.RoleName;
import ec.edu.ups.icc.proyectointegrador.users.enums.UserStatus;

public record UserResponseDto(
    Long id,
    String firstName,
    String lastName,
    String email,
    UserStatus status,
    Set<RoleName> roles,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}