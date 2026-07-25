package ec.edu.ups.icc.proyectointegrador.users.services;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import ec.edu.ups.icc.proyectointegrador.roles.enums.RoleName;
import ec.edu.ups.icc.proyectointegrador.users.dtos.UpdateUserRolesDto;
import ec.edu.ups.icc.proyectointegrador.users.dtos.UpdateUserStatusDto;
import ec.edu.ups.icc.proyectointegrador.users.dtos.UserResponseDto;
import ec.edu.ups.icc.proyectointegrador.users.enums.UserStatus;

public interface UserService {
    Page<UserResponseDto> findAll(String search, UserStatus status, RoleName role, Pageable pageable);
    UserResponseDto findById(Long id);
    UserResponseDto updateStatus(Long id, UpdateUserStatusDto dto);
    UserResponseDto updateRoles(Long id, UpdateUserRolesDto dto);
}