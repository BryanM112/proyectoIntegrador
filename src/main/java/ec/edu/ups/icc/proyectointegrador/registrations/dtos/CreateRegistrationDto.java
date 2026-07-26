package ec.edu.ups.icc.proyectointegrador.registrations.dtos;

import jakarta.validation.constraints.NotNull;

public record CreateRegistrationDto(

        @NotNull(message = "El evento es obligatorio")
        Long eventId
) {
}