package ec.edu.ups.icc.proyectointegrador.categories.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.ups.icc.proyectointegrador.categories.dtos.CategoryResponseDto;
import ec.edu.ups.icc.proyectointegrador.categories.dtos.CreateCategoryDto;
import ec.edu.ups.icc.proyectointegrador.categories.dtos.UpdateCategoryDto;
import ec.edu.ups.icc.proyectointegrador.categories.services.CategoryService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<CategoryResponseDto> findAll() {
        return service.findAllActive();
    }

    @GetMapping("/{id}")
    public CategoryResponseDto findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDto create(@Valid @RequestBody CreateCategoryDto dto) {
        return service.create(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public CategoryResponseDto update(@PathVariable Long id, @Valid @RequestBody UpdateCategoryDto dto) {
        return service.update(id, dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
