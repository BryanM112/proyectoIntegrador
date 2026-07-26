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
import ec.edu.ups.icc.proyectointegrador.core.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/categories")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
@Tag(
        name = "Categorías",
        description = "Consulta y administración de categorías de eventos académicos"
)
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @Operation(
            summary = "Listar categorías activas",
            description = """
                    Devuelve todas las categorías activas disponibles
                    para clasificar eventos académicos.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categorías obtenidas correctamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado"
            )
    })
    @GetMapping
    public List<CategoryResponseDto> findAll() {
        return service.findAllActive();
    }

    @Operation(
            summary = "Consultar una categoría por ID",
            description = """
                    Devuelve la información de una categoría activa
                    mediante su identificador interno.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoría encontrada",
                    content = @Content(
                            schema = @Schema(
                                    implementation = CategoryResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoría no encontrada"
            )
    })
    @GetMapping("/{id}")
    public CategoryResponseDto findById(
            @Parameter(
                    description = "Identificador de la categoría",
                    example = "1"
            )
            @PathVariable
            Long id
    ) {
        return service.findById(id);
    }

    @Operation(
            summary = "Crear una categoría",
            description = """
                    Crea una nueva categoría para clasificar eventos.
                    Solo puede realizar esta operación un administrador.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Categoría creada correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = CategoryResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Los datos de la categoría son inválidos"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Ya existe una categoría con esos datos"
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDto create(
            @Valid
            @RequestBody
            CreateCategoryDto dto
    ) {
        return service.create(dto);
    }

    @Operation(
            summary = "Actualizar una categoría",
            description = """
                    Actualiza completamente los datos de una categoría.
                    Solo puede realizar esta operación un administrador.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoría actualizada correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = CategoryResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Los datos de la categoría son inválidos"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoría no encontrada"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Existe otra categoría con esos datos"
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public CategoryResponseDto update(
            @Parameter(
                    description = "Identificador de la categoría",
                    example = "1"
            )
            @PathVariable
            Long id,

            @Valid
            @RequestBody
            UpdateCategoryDto dto
    ) {
        return service.update(id, dto);
    }

    @Operation(
            summary = "Eliminar una categoría",
            description = """
                    Realiza la eliminación lógica de una categoría.
                    Solo puede realizar esta operación un administrador.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Categoría eliminada correctamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene permisos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoría no encontrada"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La categoría no puede eliminarse porque está siendo utilizada"
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(
                    description = "Identificador de la categoría",
                    example = "1"
            )
            @PathVariable
            Long id
    ) {
        service.delete(id);
    }
}