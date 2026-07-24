package ec.edu.ups.icc.proyectointegrador.roles.dtos;

import java.time.OffsetDateTime;

import ec.edu.ups.icc.proyectointegrador.roles.enums.RoleName;

public record RoleResponseDto (
    Long id,
    RoleName name,
    String description,
    OffsetDateTime createdAt){
    
}
