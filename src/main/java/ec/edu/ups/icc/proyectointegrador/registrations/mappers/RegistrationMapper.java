package ec.edu.ups.icc.proyectointegrador.registrations.mappers;

import org.springframework.stereotype.Component;

import ec.edu.ups.icc.proyectointegrador.registrations.dtos.RegistrationResponseDto;
import ec.edu.ups.icc.proyectointegrador.registrations.entities.RegistrationEntity;

@Component
public class RegistrationMapper {

    public RegistrationResponseDto toResponseDto(
            RegistrationEntity entity
    ) {
        return new RegistrationResponseDto(
                entity.getId(),
                entity.getRegistrationCode(),
                entity.getEvent().getId(),
                entity.getEvent().getTitle(),
                entity.getParticipant().getId(),
                entity.getParticipant().getFirstName() + " " + entity.getParticipant().getLastName(),
                entity.getParticipant().getEmail(),
                entity.getStatus(),
                entity.getRegisteredAt(),
                entity.getStatusUpdatedAt(),
                entity.getConfirmedAt(),
                entity.getCancelledAt(),
                entity.getVersion()
        );
    }
}