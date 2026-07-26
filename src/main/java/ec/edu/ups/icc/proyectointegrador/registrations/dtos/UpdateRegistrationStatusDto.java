package ec.edu.ups.icc.proyectointegrador.registrations.dtos;

import ec.edu.ups.icc.proyectointegrador.registrations.enums.RegistrationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateRegistrationStatusDto(

        @NotNull(message = "El estado es obligatorio")
        RegistrationStatus status,

        @NotNull(message = "La versión es obligatoria")
        Long version
) {
}