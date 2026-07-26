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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/sessions")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class SessionController {

    private final SessionService service;

    public SessionController(SessionService service) {
        this.service = service;
    }

    @GetMapping("/event/{eventId}")
    public List<SessionResponseDto> findByEvent(
            @PathVariable Long eventId,
            Principal principal
    ) {
        return service.findByEvent(
                eventId,
                principal.getName()
        );
    }

    @GetMapping("/{id}")
    public SessionResponseDto findById(
            @PathVariable Long id,
            Principal principal
    ) {
        return service.findById(
                id,
                principal.getName()
        );
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<SessionResponseDto> create(
            @Valid @RequestBody CreateSessionDto dto,
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

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public SessionResponseDto update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSessionDto dto,
            Principal principal
    ) {
        return service.update(
                id,
                dto,
                principal.getName()
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            Principal principal
    ) {
        service.delete(
                id,
                principal.getName()
        );

        return ResponseEntity.noContent().build();
    }
}