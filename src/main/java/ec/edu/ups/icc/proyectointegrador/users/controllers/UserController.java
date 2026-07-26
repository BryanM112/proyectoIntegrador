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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
@Tag(
        name = "Usuarios",
        description = "Administración de usuarios, estados y roles"
)
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @Operation(
            summary = "Listar usuarios",
            description = """
                    Devuelve una lista paginada de usuarios.
                    Permite filtrar por texto, estado y rol.
                    Solo puede acceder un administrador.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuarios obtenidos correctamente"
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
    public Page<UserResponseDto> findAll(
            @Parameter(
                    description = "Texto para buscar usuarios",
                    example = "Bryan"
            )
            @RequestParam(required = false)
            String search,

            @Parameter(
                    description = "Estado del usuario",
                    example = "ACTIVE"
            )
            @RequestParam(required = false)
            UserStatus status,

            @Parameter(
                    description = "Rol del usuario",
                    example = "PARTICIPANT"
            )
            @RequestParam(required = false)
            RoleName role,

            @Parameter(
                    description = "Configuración de paginación y ordenamiento"
            )
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "id"
            )
            Pageable pageable
    ) {
        return service.findAll(
                search,
                status,
                role,
                pageable
        );
    }

    @Operation(
            summary = "Consultar un usuario por ID",
            description = """
                    Devuelve la información de un usuario mediante
                    su identificador interno.
                    Solo puede acceder un administrador.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario encontrado",
                    content = @Content(
                            schema = @Schema(
                                    implementation = UserResponseDto.class
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
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public UserResponseDto findById(
            @Parameter(
                    description = "Identificador del usuario",
                    example = "1"
            )
            @PathVariable
            Long id
    ) {
        return service.findById(id);
    }

    @Operation(
            summary = "Actualizar el estado de un usuario",
            description = """
                    Modifica el estado de un usuario.
                    Solo puede realizar esta operación un administrador.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado actualizado correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = UserResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos o transición de estado inválidos"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public UserResponseDto updateStatus(
            @Parameter(
                    description = "Identificador del usuario",
                    example = "1"
            )
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            UpdateUserStatusDto dto
    ) {
        return service.updateStatus(id, dto);
    }

    @Operation(
            summary = "Actualizar los roles de un usuario",
            description = """
                    Reemplaza los roles asignados a un usuario.
                    Solo puede realizar esta operación un administrador.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Roles actualizados correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = UserResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "La solicitud contiene roles inválidos"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario o rol no encontrado"
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/roles")
    public UserResponseDto updateRoles(
            @Parameter(
                    description = "Identificador del usuario",
                    example = "1"
            )
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            UpdateUserRolesDto dto
    ) {
        return service.updateRoles(id, dto);
    }
}