package ec.edu.ups.icc.proyectointegrador.registrations.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.ups.icc.proyectointegrador.registrations.dtos.CancelRegistrationDto;
import ec.edu.ups.icc.proyectointegrador.registrations.dtos.CreateRegistrationDto;
import ec.edu.ups.icc.proyectointegrador.registrations.dtos.RegistrationResponseDto;
import ec.edu.ups.icc.proyectointegrador.registrations.dtos.UpdateRegistrationStatusDto;
import ec.edu.ups.icc.proyectointegrador.registrations.enums.RegistrationStatus;
import ec.edu.ups.icc.proyectointegrador.registrations.services.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/registrations")
@Tag(
        name = "Inscripciones",
        description = "Gestión de inscripciones de participantes en eventos académicos"
)
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(
            RegistrationService registrationService
    ) {
        this.registrationService = registrationService;
    }

    @Operation(
            summary = "Crear una inscripción",
            description = """
                    Inscribe al usuario autenticado en un evento publicado.
                    El evento debe encontrarse dentro del periodo de inscripción
                    y debe disponer de cupos.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Inscripción creada correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = RegistrationResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El evento no permite inscripciones"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Evento no encontrado"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El participante ya está inscrito"
            )
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RegistrationResponseDto> create(
            @Valid @RequestBody CreateRegistrationDto dto,
            Authentication authentication
    ) {
        RegistrationResponseDto response =
                registrationService.create(
                        dto,
                        authentication.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Listar mis inscripciones",
            description = "Devuelve las inscripciones pertenecientes al usuario autenticado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Inscripciones obtenidas correctamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado"
            )
    })
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RegistrationResponseDto>>
            findMyRegistrations(
                    Authentication authentication
            ) {

        List<RegistrationResponseDto> response =
                registrationService.findMyRegistrations(
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Consultar una inscripción por ID",
            description = """
                    Permite consultar una inscripción al participante propietario,
                    al organizador propietario del evento o a un administrador.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Inscripción encontrada"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inscripción no encontrada"
            )
    })
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RegistrationResponseDto> findById(
            @Parameter(
                    description = "Identificador interno de la inscripción",
                    example = "1"
            )
            @PathVariable Long id,
            Authentication authentication
    ) {
        RegistrationResponseDto response =
                registrationService.findById(
                        id,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Consultar una inscripción por código",
            description = """
                    Consulta una inscripción mediante su código UUID público.
                    Solo puede acceder el participante, el organizador del evento
                    o un administrador.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Inscripción encontrada"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inscripción no encontrada"
            )
    })
    @GetMapping("/code/{registrationCode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RegistrationResponseDto> findByCode(
            @Parameter(
                    description = "Código UUID público de la inscripción",
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable UUID registrationCode,
            Authentication authentication
    ) {
        RegistrationResponseDto response =
                registrationService.findByCode(
                        registrationCode,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Listar inscripciones de un evento",
            description = """
                    Devuelve las inscripciones de un evento.
                    El acceso está permitido al organizador propietario
                    del evento y a los administradores.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Inscripciones obtenidas correctamente"
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
            )
    })
    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<List<RegistrationResponseDto>>
            findByEvent(
                    @Parameter(
                            description = "Identificador del evento",
                            example = "10"
                    )
                    @PathVariable Long eventId,

                    @Parameter(
                            description = "Estado opcional para filtrar las inscripciones",
                            example = "PENDING"
                    )
                    @RequestParam(required = false)
                    RegistrationStatus status,

                    Authentication authentication
            ) {

        List<RegistrationResponseDto> response =
                registrationService.findByEvent(
                        eventId,
                        status,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Actualizar el estado de una inscripción",
            description = """
                    Permite que el organizador propietario del evento o un
                    administrador confirme o rechace una inscripción pendiente.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado actualizado correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Transición de estado no permitida"
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
                    description = "Inscripción no encontrada"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto de versión"
            )
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<RegistrationResponseDto>
            updateStatus(
                    @Parameter(
                            description = "Identificador de la inscripción",
                            example = "1"
                    )
                    @PathVariable Long id,

                    @Valid
                    @RequestBody
                    UpdateRegistrationStatusDto dto,

                    Authentication authentication
            ) {

        RegistrationResponseDto response =
                registrationService.updateStatus(
                        id,
                        dto,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Cancelar una inscripción",
            description = """
                    Cancela una inscripción pendiente o confirmada.
                    Puede realizar la operación el participante propietario
                    o un administrador. El cupo reservado se devuelve al evento.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Inscripción cancelada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "La inscripción no puede cancelarse"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inscripción no encontrada"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflicto de versión"
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RegistrationResponseDto> cancel(
            @Parameter(
                    description = "Identificador de la inscripción",
                    example = "1"
            )
            @PathVariable Long id,

            @Valid
            @RequestBody
            CancelRegistrationDto dto,

            Authentication authentication
    ) {
        RegistrationResponseDto response =
                registrationService.cancel(
                        id,
                        dto,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }
}