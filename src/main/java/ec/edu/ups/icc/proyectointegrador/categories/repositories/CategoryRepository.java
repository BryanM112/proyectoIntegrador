package ec.edu.ups.icc.proyectointegrador.categories.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ec.edu.ups.icc.proyectointegrador.categories.entities.CategoryEntity;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    List<CategoryEntity> findAllByActiveTrueOrderByNameAsc();

    Optional<CategoryEntity> findByIdAndActiveTrue(Long id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
