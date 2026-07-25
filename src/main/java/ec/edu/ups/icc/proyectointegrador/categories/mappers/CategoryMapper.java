package ec.edu.ups.icc.proyectointegrador.categories.mappers;

import org.springframework.stereotype.Component;

import ec.edu.ups.icc.proyectointegrador.categories.dtos.CategoryResponseDto;
import ec.edu.ups.icc.proyectointegrador.categories.dtos.CreateCategoryDto;
import ec.edu.ups.icc.proyectointegrador.categories.dtos.UpdateCategoryDto;
import ec.edu.ups.icc.proyectointegrador.categories.entities.CategoryEntity;

@Component
public class CategoryMapper {

    public CategoryEntity toEntity(CreateCategoryDto dto) {
        CategoryEntity entity = new CategoryEntity();

        entity.setName(dto.name());
        entity.setDescription(dto.description());

        return entity;
    }

    public void updateEntity(CategoryEntity entity, UpdateCategoryDto dto) {
        entity.setName(dto.name());
        entity.setDescription(dto.description());
    }

    public CategoryResponseDto toResponseDto(CategoryEntity entity) {
        return new CategoryResponseDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
