package ec.edu.ups.icc.proyectointegrador.users.controllers;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.ups.icc.proyectointegrador.roles.enums.RoleName;
import ec.edu.ups.icc.proyectointegrador.users.dtos.UpdateUserRolesDto;
import ec.edu.ups.icc.proyectointegrador.users.dtos.UpdateUserStatusDto;
import ec.edu.ups.icc.proyectointegrador.users.dtos.UserResponseDto;
import ec.edu.ups.icc.proyectointegrador.users.enums.UserStatus;
import ec.edu.ups.icc.proyectointegrador.users.services.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<UserResponseDto> findAll(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) UserStatus status,
        @RequestParam(required = false) RoleName role,
        @PageableDefault(
        page = 0,
        size = 10,
        sort = "id"
    )Pageable pageable) {
        return service.findAll(search, status, role, pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public UserResponseDto findById(@PathVariable Long id) {
        return service.findById(id);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public UserResponseDto updateStatus(@PathVariable Long id,@Valid @RequestBody UpdateUserStatusDto dto) {
        return service.updateStatus(id, dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/roles")
    public UserResponseDto updateRoles(@PathVariable Long id, @Valid @RequestBody UpdateUserRolesDto dto) {
        return service.updateRoles(id, dto);
    }
}