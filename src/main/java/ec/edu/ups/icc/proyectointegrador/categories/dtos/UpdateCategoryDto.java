package ec.edu.ups.icc.proyectointegrador.categories.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCategoryDto(

        @NotBlank(message = "El nombre de la categoría es obligatorio")
        @Size(max = 80, message = "El nombre no puede superar los 80 caracteres")
        String name,
        @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
        String description

) {
}