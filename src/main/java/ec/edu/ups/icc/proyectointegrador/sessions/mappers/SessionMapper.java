package ec.edu.ups.icc.proyectointegrador.sessions.mappers;

import org.springframework.stereotype.Component;

import ec.edu.ups.icc.proyectointegrador.sessions.dtos.CreateSessionDto;
import ec.edu.ups.icc.proyectointegrador.sessions.dtos.SessionResponseDto;
import ec.edu.ups.icc.proyectointegrador.sessions.dtos.UpdateSessionDto;
import ec.edu.ups.icc.proyectointegrador.sessions.entities.SessionEntity;

@Component
public class SessionMapper {

    public SessionEntity toEntity(CreateSessionDto dto) {
        SessionEntity entity = new SessionEntity();

        entity.setTitle(dto.title().trim());
        entity.setDescription(normalizeOptionalText(dto.description()));
        entity.setStartAt(dto.startAt());
        entity.setEndAt(dto.endAt());
        entity.setLocation(normalizeOptionalText(dto.location()));
        entity.setVirtualUrl(normalizeOptionalText(dto.virtualUrl()));

        return entity;
    }

    public void updateEntity(
            SessionEntity entity,
            UpdateSessionDto dto
    ) {
        entity.setTitle(dto.title().trim());
        entity.setDescription(normalizeOptionalText(dto.description()));
        entity.setStartAt(dto.startAt());
        entity.setEndAt(dto.endAt());
        entity.setLocation(normalizeOptionalText(dto.location()));
        entity.setVirtualUrl(normalizeOptionalText(dto.virtualUrl()));
    }

    public SessionResponseDto toResponseDto(SessionEntity entity) {
        return new SessionResponseDto(
                entity.getId(),
                entity.getEvent().getId(),
                entity.getEvent().getTitle(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStartAt(),
                entity.getEndAt(),
                entity.getLocation(),
                entity.getVirtualUrl(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isBlank() ? null : normalized;
    }
}