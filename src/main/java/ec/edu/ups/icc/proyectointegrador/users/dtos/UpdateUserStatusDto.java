package ec.edu.ups.icc.proyectointegrador.users.dtos;

import ec.edu.ups.icc.proyectointegrador.users.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusDto (
    @NotNull(message = "El estado de usuario es obligatorio") 
    UserStatus status
){
    
}
