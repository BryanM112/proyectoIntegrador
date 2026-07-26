package ec.edu.ups.icc.proyectointegrador.events.dtos;

import java.time.OffsetDateTime;

import ec.edu.ups.icc.proyectointegrador.events.enums.EventModality;
import ec.edu.ups.icc.proyectointegrador.events.enums.EventStatus;

public record EventResponseDto(
        Long id,
        String title,
        String description,
        EventModality modality,
        String location,
        String virtualUrl,
        Integer capacity,
        Integer availableCapacity,
        OffsetDateTime registrationStartAt,
        OffsetDateTime registrationEndAt,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        EventStatus status,
        Long organizerId,
        String organizerName,
        Long categoryId,
        String categoryName,
        Boolean deleted,
        Long version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
