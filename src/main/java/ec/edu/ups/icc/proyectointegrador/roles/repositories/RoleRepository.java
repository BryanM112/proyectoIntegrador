package ec.edu.ups.icc.proyectointegrador.roles.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ec.edu.ups.icc.proyectointegrador.roles.entities.RoleEntity;
import ec.edu.ups.icc.proyectointegrador.roles.enums.RoleName;

public interface RoleRepository extends JpaRepository<RoleEntity, Long>{
    Optional<RoleEntity> findByName(RoleName name);
    boolean existsByName(RoleName name);
}
