package ec.edu.ups.icc.proyectointegrador.roles.repositories;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import ec.edu.ups.icc.proyectointegrador.roles.entities.RoleEntity;
import ec.edu.ups.icc.proyectointegrador.roles.enums.RoleName;

public interface RoleRepository extends JpaRepository<RoleEntity, Long>{
    Optional<RoleEntity> findByName(RoleName name);
    boolean existsByName(RoleName name);
    List<RoleEntity> findAllByNameIn(Set<RoleName> names);
}
