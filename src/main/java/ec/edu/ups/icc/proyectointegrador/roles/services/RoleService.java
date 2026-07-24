package ec.edu.ups.icc.proyectointegrador.roles.services;

import java.util.List;

import ec.edu.ups.icc.proyectointegrador.roles.dtos.RoleResponseDto;

public interface RoleService {
    List<RoleResponseDto> findAll();
}
