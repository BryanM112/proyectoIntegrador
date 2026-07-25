package ec.edu.ups.icc.proyectointegrador.users.mappers;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ec.edu.ups.icc.proyectointegrador.users.dtos.UserResponseDto;
import ec.edu.ups.icc.proyectointegrador.users.entities.UserEntity;

@Component
public class UserMapper {
    public UserResponseDto toResponseDto(UserEntity entity) {
        return new UserResponseDto(
            entity.getId(),
            entity.getFirstName(),
            entity.getLastName(),
            entity.getEmail(),
            entity.getStatus(),
            entity.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet()),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}