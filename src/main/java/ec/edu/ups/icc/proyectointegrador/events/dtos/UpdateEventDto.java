package ec.edu.ups.icc.proyectointegrador.events.dtos;

import java.time.OffsetDateTime;

import ec.edu.ups.icc.proyectointegrador.events.enums.EventModality;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateEventDto(

        @NotBlank(message = "El título del evento es obligatorio")
        @Size(max = 160, message = "El título no puede superar los 160 caracteres")
        String title,

        @NotBlank(message = "La descripción del evento es obligatoria")
        String description,

        @NotNull(message = "La modalidad del evento es obligatoria")
        EventModality modality,

        @Size(max = 200, message = "La ubicación no puede superar los 200 caracteres")
        String location,

        @Size(max = 500, message = "La URL virtual no puede superar los 500 caracteres")
        String virtualUrl,

        @NotNull(message = "La capacidad es obligatoria")
        @Positive(message = "La capacidad debe ser mayor que cero")
        Integer capacity,

        @NotNull(message = "La fecha de inicio de inscripciones es obligatoria")
        OffsetDateTime registrationStartAt,

        @NotNull(message = "La fecha de fin de inscripciones es obligatoria")
        OffsetDateTime registrationEndAt,

        @NotNull(message = "La fecha de inicio del evento es obligatoria")
        OffsetDateTime startAt,

        @NotNull(message = "La fecha de finalización del evento es obligatoria")
        OffsetDateTime endAt,

        @NotNull(message = "La categoría es obligatoria")
        @Positive(message = "El identificador de la categoría debe ser válido")
        Long categoryId,

        @NotNull(message = "La versión del evento es obligatoria")
        @PositiveOrZero(message = "La versión no puede ser negativa")
        Long version

) {
}
