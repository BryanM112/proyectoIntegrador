package ec.edu.ups.icc.proyectointegrador.users.services.impl;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.ups.icc.proyectointegrador.core.exceptions.BusinessRuleException;
import ec.edu.ups.icc.proyectointegrador.core.exceptions.ResourceNotFoundException;
import ec.edu.ups.icc.proyectointegrador.roles.entities.RoleEntity;
import ec.edu.ups.icc.proyectointegrador.roles.enums.RoleName;
import ec.edu.ups.icc.proyectointegrador.roles.repositories.RoleRepository;
import ec.edu.ups.icc.proyectointegrador.users.dtos.UpdateUserRolesDto;
import ec.edu.ups.icc.proyectointegrador.users.dtos.UpdateUserStatusDto;
import ec.edu.ups.icc.proyectointegrador.users.dtos.UserResponseDto;
import ec.edu.ups.icc.proyectointegrador.users.entities.UserEntity;
import ec.edu.ups.icc.proyectointegrador.users.enums.UserStatus;
import ec.edu.ups.icc.proyectointegrador.users.mappers.UserMapper;
import ec.edu.ups.icc.proyectointegrador.users.repositories.UserRepository;
import ec.edu.ups.icc.proyectointegrador.users.services.UserService;
import ec.edu.ups.icc.proyectointegrador.users.specifications.UserSpecifications;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final UserMapper mapper;
    private final RoleRepository roleRepository;

    

    public UserServiceImpl(UserRepository repository, UserMapper mapper, RoleRepository roleRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDto> findAll(String search, UserStatus status, RoleName role, Pageable pageable) {
        Specification<UserEntity> specification =
                Specification.where(UserSpecifications.hasSearch(search))
                        .and(UserSpecifications.hasStatus(status))
                        .and(UserSpecifications.hasRole(role));

        return repository.findAll(specification, pageable).map(mapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto findById(Long id) {
        return repository.findWithRolesById(id)
            .map(mapper::toResponseDto)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    @Override
    @Transactional
    public UserResponseDto updateStatus(Long id, UpdateUserStatusDto dto) {
        UserEntity user = repository.findWithRolesById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        user.setStatus(dto.status());
        user.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        UserEntity updatedUser = repository.save(user);
        return mapper.toResponseDto(updatedUser);

    }

    @Override
    @Transactional
    public UserResponseDto updateRoles(Long id, UpdateUserRolesDto dto) {

        UserEntity user = repository.findWithRolesById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<RoleEntity> foundRoles = roleRepository.findAllByNameIn(dto.roles());

        if (foundRoles.size() != dto.roles().size()) {
            throw new BusinessRuleException(
                "Uno o varios roles no existen"
            );
        }

    user.setRoles(new HashSet<>(foundRoles));

    user.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

    UserEntity updatedUser = repository.save(user);

    return mapper.toResponseDto(updatedUser);
    }
}