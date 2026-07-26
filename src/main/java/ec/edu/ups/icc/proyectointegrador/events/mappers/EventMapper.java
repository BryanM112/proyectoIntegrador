package ec.edu.ups.icc.proyectointegrador.events.mappers;

import org.springframework.stereotype.Component;

import ec.edu.ups.icc.proyectointegrador.events.dtos.CreateEventDto;
import ec.edu.ups.icc.proyectointegrador.events.dtos.EventResponseDto;
import ec.edu.ups.icc.proyectointegrador.events.dtos.UpdateEventDto;
import ec.edu.ups.icc.proyectointegrador.events.entities.EventEntity;

@Component
public class EventMapper {

    public EventEntity toEntity(CreateEventDto dto) {
        EventEntity entity = new EventEntity();

        entity.setTitle(dto.title());
        entity.setDescription(dto.description());
        entity.setModality(dto.modality());
        entity.setLocation(dto.location());
        entity.setVirtualUrl(dto.virtualUrl());
        entity.setCapacity(dto.capacity());
        entity.setRegistrationStartAt(dto.registrationStartAt());
        entity.setRegistrationEndAt(dto.registrationEndAt());
        entity.setStartAt(dto.startAt());
        entity.setEndAt(dto.endAt());

        return entity;
    }

    public void updateEntity(EventEntity entity, UpdateEventDto dto) {
        entity.setTitle(dto.title());
        entity.setDescription(dto.description());
        entity.setModality(dto.modality());
        entity.setLocation(dto.location());
        entity.setVirtualUrl(dto.virtualUrl());
        entity.setCapacity(dto.capacity());
        entity.setRegistrationStartAt(dto.registrationStartAt());
        entity.setRegistrationEndAt(dto.registrationEndAt());
        entity.setStartAt(dto.startAt());
        entity.setEndAt(dto.endAt());
    }

    public EventResponseDto toResponseDto(EventEntity entity) {
        String organizerName =
                entity.getOrganizer().getFirstName()
                + " "
                + entity.getOrganizer().getLastName();

        return new EventResponseDto(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getModality(),
                entity.getLocation(),
                entity.getVirtualUrl(),
                entity.getCapacity(),
                entity.getAvailableCapacity(),
                entity.getRegistrationStartAt(),
                entity.getRegistrationEndAt(),
                entity.getStartAt(),
                entity.getEndAt(),
                entity.getStatus(),
                entity.getOrganizer().getId(),
                organizerName,
                entity.getCategory().getId(),
                entity.getCategory().getName(),
                entity.getDeleted(),
                entity.getVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
