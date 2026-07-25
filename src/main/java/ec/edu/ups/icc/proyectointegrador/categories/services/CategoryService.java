package ec.edu.ups.icc.proyectointegrador.categories.services;

import java.util.List;

import ec.edu.ups.icc.proyectointegrador.categories.dtos.CategoryResponseDto;
import ec.edu.ups.icc.proyectointegrador.categories.dtos.CreateCategoryDto;
import ec.edu.ups.icc.proyectointegrador.categories.dtos.UpdateCategoryDto;

public interface CategoryService {

    List<CategoryResponseDto> findAllActive();

    CategoryResponseDto findById(Long id);

    CategoryResponseDto create(CreateCategoryDto dto);

    CategoryResponseDto update(Long id, UpdateCategoryDto dto);

    void delete(Long id);
}
