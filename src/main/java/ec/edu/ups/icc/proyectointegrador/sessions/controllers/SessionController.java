package ec.edu.ups.icc.proyectointegrador.sessions.controllers;

import java.net.URI;
import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import ec.edu.ups.icc.proyectointegrador.core.config.OpenApiConfig;
import ec.edu.ups.icc.proyectointegrador.sessions.dtos.CreateSessionDto;
import ec.edu.ups.icc.proyectointegrador.sessions.dtos.SessionResponseDto;
import ec.edu.ups.icc.proyectointegrador.sessions.dtos.UpdateSessionDto;
import ec.edu.ups.icc.proyectointegrador.sessions.services.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/sessions")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
@Tag(
        name = "Sesiones",
        description = "Gestión de las sesiones que forman parte de los eventos académicos"
)
public class SessionController {

    private final SessionService service;

    public SessionController(SessionService service) {
        this.service = service;
    }

    @Operation(
            summary = "Listar sesiones de un evento",
            description = """
                    Devuelve todas las sesiones asociadas a un evento.

                    La visibilidad depende del estado del evento y del usuario
                    autenticado. Los participantes pueden consultar sesiones de
                    eventos publicados, mientras que el administrador y el
                    organizador propietario pueden consultar sus eventos.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sesiones obtenidas correctamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Evento no encontrado o no visible para el usuario"
            )
    })
    @GetMapping("/event/{eventId}")
    public List<SessionResponseDto> findByEvent(
            @Parameter(
                    description = "Identificador del evento",
                    example = "10"
            )
            @PathVariable
            Long eventId,

            Principal principal
    ) {
        return service.findByEvent(
                eventId,
                principal.getName()
        );
    }

    @Operation(
            summary = "Consultar una sesión por ID",
            description = """
                    Devuelve la información completa de una sesión.

                    El usuario debe tener acceso al evento al que pertenece
                    la sesión.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sesión encontrada",
                    content = @Content(
                            schema = @Schema(
                                    implementation = SessionResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Sesión no encontrada o no visible para el usuario"
            )
    })
    @GetMapping("/{id}")
    public SessionResponseDto findById(
            @Parameter(
                    description = "Identificador de la sesión",
                    example = "1"
            )
            @PathVariable
            Long id,

            Principal principal
    ) {
        return service.findById(
                id,
                principal.getName()
        );
    }

    @Operation(
            summary = "Crear una sesión",
            description = """
                    Crea una sesión dentro de un evento.

                    Solo puede realizar esta operación un administrador o el
                    organizador propietario del evento.

                    Las fechas de la sesión deben estar dentro del periodo del
                    evento y no pueden solaparse con otra sesión existente.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Sesión creada correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = SessionResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o fechas fuera del evento"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos para modificar el evento"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Evento no encontrado"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La sesión está duplicada o se solapa con otra sesión"
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<SessionResponseDto> create(
            @Valid
            @RequestBody
            CreateSessionDto dto,

            Principal principal
    ) {
        SessionResponseDto response = service.create(
                dto,
                principal.getName()
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @Operation(
            summary = "Actualizar una sesión",
            description = """
                    Actualiza completamente los datos de una sesión.

                    Solo puede realizar esta operación un administrador o el
                    organizador propietario del evento.

                    Las nuevas fechas deben permanecer dentro del periodo del
                    evento y no pueden solaparse con otras sesiones.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sesión actualizada correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = SessionResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o fechas fuera del evento"
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
                    description = "Sesión o evento no encontrado"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La sesión está duplicada o se solapa con otra sesión"
            )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public SessionResponseDto update(
            @Parameter(
                    description = "Identificador de la sesión",
                    example = "1"
            )
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            UpdateSessionDto dto,

            Principal principal
    ) {
        return service.update(
                id,
                dto,
                principal.getName()
        );
    }

    @Operation(
            summary = "Eliminar una sesión",
            description = """
                    Elimina físicamente una sesión.

                    Solo puede realizar esta operación un administrador o el
                    organizador propietario del evento.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Sesión eliminada correctamente"
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
                    description = "Sesión no encontrada"
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Void> delete(
            @Parameter(
                    description = "Identificador de la sesión",
                    example = "1"
            )
            @PathVariable
            Long id,

            Principal principal
    ) {
        service.delete(
                id,
                principal.getName()
        );

        return ResponseEntity.noContent().build();
    }
}