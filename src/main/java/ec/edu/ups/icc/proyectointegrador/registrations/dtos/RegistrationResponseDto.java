package ec.edu.ups.icc.proyectointegrador.registrations.dtos;

import java.time.OffsetDateTime;
import java.util.UUID;

import ec.edu.ups.icc.proyectointegrador.registrations.enums.RegistrationStatus;

public record RegistrationResponseDto(
        Long id,
        UUID registrationCode,
        Long eventId,
        String eventTitle,
        Long participantId,
        String participantName,
        String participantEmail,
        RegistrationStatus status,
        OffsetDateTime registeredAt,
        OffsetDateTime statusUpdatedAt,
        OffsetDateTime confirmedAt,
        OffsetDateTime cancelledAt,
        Long version
) {
}