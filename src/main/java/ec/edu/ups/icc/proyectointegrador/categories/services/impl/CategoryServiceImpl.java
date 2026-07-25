package ec.edu.ups.icc.proyectointegrador.categories.services.impl;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.ups.icc.proyectointegrador.categories.dtos.CategoryResponseDto;
import ec.edu.ups.icc.proyectointegrador.categories.dtos.CreateCategoryDto;
import ec.edu.ups.icc.proyectointegrador.categories.dtos.UpdateCategoryDto;
import ec.edu.ups.icc.proyectointegrador.categories.entities.CategoryEntity;
import ec.edu.ups.icc.proyectointegrador.categories.mappers.CategoryMapper;
import ec.edu.ups.icc.proyectointegrador.categories.repositories.CategoryRepository;
import ec.edu.ups.icc.proyectointegrador.categories.services.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    public CategoryServiceImpl(
            CategoryRepository repository,
            CategoryMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> findAllActive() {
        return repository
                .findAllByActiveTrueOrderByNameAsc()
                .stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDto findById(Long id) {
        CategoryEntity category = repository
                .findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalStateException("Categoría no encontrada"));

        return mapper.toResponseDto(category);
    }

    @Override
    @Transactional
    public CategoryResponseDto create(CreateCategoryDto dto) {
        String normalizedName = normalizeRequiredText(
                dto.name()
        );

        String normalizedDescription = normalizeOptionalText(
                dto.description()
        );

        if (repository.existsByNameIgnoreCase(normalizedName)) {
            throw new IllegalStateException("Ya existe una categoría con ese nombre");
        }

        CategoryEntity category = mapper.toEntity(dto);

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        category.setName(normalizedName);
        category.setDescription(normalizedDescription);
        category.setActive(true);
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        CategoryEntity savedCategory = repository.save(category);

        return mapper.toResponseDto(savedCategory);
    }

    @Override
    @Transactional
    public CategoryResponseDto update(Long id, UpdateCategoryDto dto) {
        CategoryEntity category = repository
                .findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalStateException("Categoría no encontrada"));

        String normalizedName = normalizeRequiredText(
                dto.name()
        );

        String normalizedDescription = normalizeOptionalText(
                dto.description()
        );

        boolean duplicatedName = repository.existsByNameIgnoreCaseAndIdNot(normalizedName, id);

        if (duplicatedName) {
            throw new IllegalStateException("Ya existe una categoría con ese nombre");
        }

        mapper.updateEntity(category, dto);

        category.setName(normalizedName);
        category.setDescription(normalizedDescription);
        category.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        CategoryEntity updatedCategory = repository.save(category);

        return mapper.toResponseDto(updatedCategory);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        CategoryEntity category = repository
                .findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalStateException("Categoría no encontrada"));

        category.setActive(false);
        category.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        repository.save(category);
    }

    private String normalizeRequiredText(String value) {
        return value.trim();
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}
