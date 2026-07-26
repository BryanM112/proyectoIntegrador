package ec.edu.ups.icc.proyectointegrador.events.dtos;

import ec.edu.ups.icc.proyectointegrador.events.enums.EventStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateEventStatusDto(

        @NotNull(message = "El estado del evento es obligatorio")
        EventStatus status,

        @NotNull(message = "La versión del evento es obligatoria")
        @PositiveOrZero(message = "La versión no puede ser negativa")
        Long version

) {
}
