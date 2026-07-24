package ec.edu.ups.icc.proyectointegrador.roles.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.ups.icc.proyectointegrador.roles.dtos.RoleResponseDto;
import ec.edu.ups.icc.proyectointegrador.roles.mappers.RoleMapper;
import ec.edu.ups.icc.proyectointegrador.roles.repositories.RoleRepository;
import ec.edu.ups.icc.proyectointegrador.roles.services.RoleService;

@Service
public class RoleServiceImpl implements RoleService{
    private final RoleRepository repository;
    private final RoleMapper mapper;
    public RoleServiceImpl(RoleRepository repository, RoleMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
    @Override
    @Transactional(readOnly = true)
    public List<RoleResponseDto> findAll() {
        return repository.findAll().stream().map(mapper::toResponseDto).toList();
    }

    
}
