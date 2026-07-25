package ec.edu.ups.icc.proyectointegrador.users.dtos;


import java.util.Set;

import ec.edu.ups.icc.proyectointegrador.roles.enums.RoleName;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRolesDto(
        @NotNull(message = "La lista de roles es obligatoria")
        @NotEmpty(message = "El usuario debe tener al menos un rol")
        Set<RoleName> roles) {
}
