package ec.edu.ups.icc.proyectointegrador.roles.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.ups.icc.proyectointegrador.roles.dtos.RoleResponseDto;
import ec.edu.ups.icc.proyectointegrador.roles.services.RoleService;

@RestController
@RequestMapping("/roles")
public class RoleController {
    private final RoleService service;

    public RoleController(RoleService service) {
        this.service = service;
    }

    @GetMapping
    public List<RoleResponseDto> findAll() {
        return service.findAll();
    }
}
