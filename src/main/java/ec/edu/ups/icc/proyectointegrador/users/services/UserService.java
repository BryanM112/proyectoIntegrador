package ec.edu.ups.icc.proyectointegrador.users.services;

import java.util.List;

import ec.edu.ups.icc.proyectointegrador.users.dtos.UserResponseDto;

public interface UserService {
    List<UserResponseDto> findAll();
    UserResponseDto findById(Long id);
}