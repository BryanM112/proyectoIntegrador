package ec.edu.ups.icc.proyectointegrador.sessions.dtos;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateSessionDto(

        @NotBlank(message = "El título es obligatorio")
        @Size(
                max = 160,
                message = "El título no puede superar los 160 caracteres"
        )
        String title,

        String description,

        @NotNull(message = "La fecha de inicio es obligatoria")
        OffsetDateTime startAt,

        @NotNull(message = "La fecha de finalización es obligatoria")
        OffsetDateTime endAt,

        @Size(
                max = 200,
                message = "La ubicación no puede superar los 200 caracteres"
        )
        String location,

        @Size(
                max = 500,
                message = "La URL virtual no puede superar los 500 caracteres"
        )
        String virtualUrl
) {
}