package ec.edu.ups.icc.proyectointegrador.roles.mappers;

import org.springframework.stereotype.Component;

import ec.edu.ups.icc.proyectointegrador.roles.dtos.RoleResponseDto;
import ec.edu.ups.icc.proyectointegrador.roles.entities.RoleEntity;

@Component
public class RoleMapper {
    public RoleResponseDto toResponseDto(RoleEntity entity){
        return new RoleResponseDto(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getCreatedAt()
        );
    }
}
