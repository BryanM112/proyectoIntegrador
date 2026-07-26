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

import ec.edu.ups.icc.proyectointegrador.events.dtos.CreateEventDto;
import ec.edu.ups.icc.proyectointegrador.events.dtos.EventResponseDto;
import ec.edu.ups.icc.proyectointegrador.events.dtos.UpdateEventDto;
import ec.edu.ups.icc.proyectointegrador.events.dtos.UpdateEventStatusDto;
import ec.edu.ups.icc.proyectointegrador.events.enums.EventModality;
import ec.edu.ups.icc.proyectointegrador.events.enums.EventStatus;
import ec.edu.ups.icc.proyectointegrador.events.services.EventService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService service;

    public EventController(EventService service) {
        this.service = service;
    }

    @GetMapping
    public Page<EventResponseDto> findAll(
            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            EventStatus status,

            @RequestParam(required = false)
            EventModality modality,

            @RequestParam(required = false)
            Long categoryId,

            @RequestParam(required = false)
            Long organizerId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime startFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime startTo,

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

    @GetMapping("/{id}")
    public EventResponseDto findById(
            @PathVariable Long id, Principal principal
    ) {
        return service.findById(id, principal.getName());
    }

    @PreAuthorize(
            "hasAnyRole('ADMIN', 'ORGANIZER')"
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponseDto create(
            @Valid @RequestBody CreateEventDto dto,
            Principal principal
    ) {
        return service.create(
                dto,
                principal.getName()
        );
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @PutMapping("/{id}")
    public EventResponseDto update(
        @PathVariable Long id,
        @Valid @RequestBody UpdateEventDto dto,
        Principal principal
    ){
        return service.update(
            id,
            dto,
            principal.getName()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            Principal principal
    ) {
        service.delete(
                id,
                principal.getName()
        );
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @PatchMapping("/{id}/status")
    public EventResponseDto updateStatus(
        @PathVariable Long id,
        @Valid @RequestBody UpdateEventStatusDto dto,
        Principal principal
    ) {
        return service.updateStatus(
            id,
            dto,
            principal.getName()
        );
    }
}