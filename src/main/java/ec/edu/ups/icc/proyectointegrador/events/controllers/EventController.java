package ec.edu.ups.icc.proyectointegrador.events.controllers;

import java.security.Principal;
import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.ups.icc.proyectointegrador.core.config.OpenApiConfig;
import ec.edu.ups.icc.proyectointegrador.events.dtos.CreateEventDto;
import ec.edu.ups.icc.proyectointegrador.events.dtos.EventResponseDto;
import ec.edu.ups.icc.proyectointegrador.events.dtos.UpdateEventDto;
import ec.edu.ups.icc.proyectointegrador.events.dtos.UpdateEventStatusDto;
import ec.edu.ups.icc.proyectointegrador.events.enums.EventModality;
import ec.edu.ups.icc.proyectointegrador.events.enums.EventStatus;
import ec.edu.ups.icc.proyectointegrador.events.services.EventService;
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
@RequestMapping("/events")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
@Tag(
        name = "Eventos",
        description = "Consulta, creación y administración de eventos académicos"
)
public class EventController {

    private final EventService service;

    public EventController(EventService service) {
        this.service = service;
    }

    @Operation(
            summary = "Listar eventos",
            description = """
                    Devuelve una lista paginada de eventos visibles para el
                    usuario autenticado.

                    Permite filtrar por texto, estado, modalidad, categoría,
                    organizador y rango de fecha de inicio.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Eventos obtenidos correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Algún parámetro de búsqueda es inválido"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado"
            )
    })
    @GetMapping
    public Page<EventResponseDto> findAll(
            @Parameter(
                    description = "Texto para buscar en el título o descripción",
                    example = "Inteligencia artificial"
            )
            @RequestParam(required = false)
            String search,

            @Parameter(
                    description = "Estado del evento",
                    example = "PUBLISHED"
            )
            @RequestParam(required = false)
            EventStatus status,

            @Parameter(
                    description = "Modalidad del evento",
                    example = "VIRTUAL"
            )
            @RequestParam(required = false)
            EventModality modality,

            @Parameter(
                    description = "Identificador de la categoría",
                    example = "2"
            )
            @RequestParam(required = false)
            Long categoryId,

            @Parameter(
                    description = "Identificador del organizador",
                    example = "5"
            )
            @RequestParam(required = false)
            Long organizerId,

            @Parameter(
                    description = "Fecha mínima de inicio en formato ISO 8601",
                    example = "2026-08-01T00:00:00-05:00"
            )
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime startFrom,

            @Parameter(
                    description = "Fecha máxima de inicio en formato ISO 8601",
                    example = "2026-12-31T23:59:59-05:00"
            )
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime startTo,

            @Parameter(
                    description = "Configuración de paginación y ordenamiento"
            )
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "startAt"
            )
            Pageable pageable,

            Principal principal
    ) {
        return service.findAll(
                search,
                status,
                modality,
                categoryId,
                organizerId,
                startFrom,
                startTo,
                pageable,
                principal.getName()
        );
    }

    @Operation(
            summary = "Consultar un evento por ID",
            description = """
                    Devuelve la información de un evento según las reglas
                    de visibilidad aplicables al usuario autenticado.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Evento encontrado",
                    content = @Content(
                            schema = @Schema(
                                    implementation = EventResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Evento no encontrado o no visible"
            )
    })
    @GetMapping("/{id}")
    public EventResponseDto findById(
            @Parameter(
                    description = "Identificador del evento",
                    example = "10"
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
            summary = "Crear un evento",
            description = """
                    Crea un nuevo evento académico.

                    Solo puede realizar esta operación un administrador
                    o un organizador. El organizador autenticado queda
                    asociado como propietario según las reglas del servicio.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Evento creado correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = EventResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos o fechas del evento inválidos"
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
                    description = "Categoría u organizador no encontrado"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Existe un conflicto con los datos del evento"
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponseDto create(
            @Valid
            @RequestBody
            CreateEventDto dto,

            Principal principal
    ) {
        return service.create(
                dto,
                principal.getName()
        );
    }

    @Operation(
            summary = "Actualizar un evento",
            description = """
                    Actualiza completamente los datos de un evento.

                    Solo puede realizar la operación un administrador o el
                    organizador propietario del evento.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Evento actualizado correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = EventResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos, fechas o capacidad inválidos"
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
                    description = "Evento o categoría no encontrado"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto de versión o de datos"
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @PutMapping("/{id}")
    public EventResponseDto update(
            @Parameter(
                    description = "Identificador del evento",
                    example = "10"
            )
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            UpdateEventDto dto,

            Principal principal
    ) {
        return service.update(
                id,
                dto,
                principal.getName()
        );
    }

    @Operation(
            summary = "Eliminar un evento",
            description = """
                    Realiza la eliminación lógica de un evento.

                    Solo puede realizar la operación un administrador o el
                    organizador propietario del evento.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Evento eliminado correctamente"
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
                    description = "Evento no encontrado"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El evento no puede eliminarse por su estado actual"
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(
                    description = "Identificador del evento",
                    example = "10"
            )
            @PathVariable
            Long id,

            Principal principal
    ) {
        service.delete(
                id,
                principal.getName()
        );
    }

    @Operation(
            summary = "Actualizar el estado de un evento",
            description = """
                    Cambia el estado de un evento según las transiciones
                    permitidas por las reglas de negocio.

                    Solo puede realizar la operación un administrador o el
                    organizador propietario del evento.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado actualizado correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = EventResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "La transición de estado no está permitida"
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
                    description = "Evento no encontrado"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto de versión"
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @PatchMapping("/{id}/status")
    public EventResponseDto updateStatus(
            @Parameter(
                    description = "Identificador del evento",
                    example = "10"
            )
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            UpdateEventStatusDto dto,

            Principal principal
    ) {
        return service.updateStatus(
                id,
                dto,
                principal.getName()
        );
    }
}