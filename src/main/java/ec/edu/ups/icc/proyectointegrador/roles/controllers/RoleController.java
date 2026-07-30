package ec.edu.ups.icc.proyectointegrador.roles.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.ups.icc.proyectointegrador.roles.dtos.RoleResponseDto;
import ec.edu.ups.icc.proyectointegrador.roles.services.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import ec.edu.ups.icc.proyectointegrador.core.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/roles")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
@Tag(
        name = "Roles",
        description = "Consulta de los roles disponibles en el sistema"
)
public class RoleController {

    private final RoleService service;

    public RoleController(RoleService service) {
        this.service = service;
    }

    @Operation(
            summary = "Listar roles",
            description = """
                    Devuelve todos los roles disponibles en el sistema,
                    como ADMIN, ORGANIZER y PARTICIPANT.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Roles obtenidos correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = RoleResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos"
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<RoleResponseDto>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }
}