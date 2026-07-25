package ec.edu.ups.icc.proyectointegrador.categories.dtos;

import java.time.OffsetDateTime;

public record CategoryResponseDto(
        Long id,
        String name,
        String description,
        Boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
