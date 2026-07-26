package ec.edu.ups.icc.proyectointegrador.events.services;

import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import ec.edu.ups.icc.proyectointegrador.events.dtos.CreateEventDto;
import ec.edu.ups.icc.proyectointegrador.events.dtos.EventResponseDto;
import ec.edu.ups.icc.proyectointegrador.events.dtos.UpdateEventDto;
import ec.edu.ups.icc.proyectointegrador.events.dtos.UpdateEventStatusDto;
import ec.edu.ups.icc.proyectointegrador.events.enums.EventModality;
import ec.edu.ups.icc.proyectointegrador.events.enums.EventStatus;

public interface EventService {

    Page<EventResponseDto> findAll(
        String search,
        EventStatus status,
        EventModality modality,
        Long categoryId,
        Long organizerId,
        OffsetDateTime startFrom,
        OffsetDateTime startTo,
        Pageable pageable,
        String authenticatedEmail
    );

    EventResponseDto findById(Long id, String authenticatedEmail);

    EventResponseDto create(CreateEventDto dto,String authenticatedEmail);

    EventResponseDto update(Long id, UpdateEventDto dto, String authenticatedEmail);

    void delete(Long id, String authenticatedEmail);

    EventResponseDto updateStatus(Long id, UpdateEventStatusDto dto, String authenticatedEmail);
}
