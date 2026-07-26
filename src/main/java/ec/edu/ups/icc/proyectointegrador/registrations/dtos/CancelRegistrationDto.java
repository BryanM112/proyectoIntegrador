package ec.edu.ups.icc.proyectointegrador.registrations.dtos;

import jakarta.validation.constraints.NotNull;

public record CancelRegistrationDto(

        @NotNull(message = "La versión es obligatoria")
        Long version
) {
}