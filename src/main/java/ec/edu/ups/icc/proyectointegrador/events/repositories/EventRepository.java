package ec.edu.ups.icc.proyectointegrador.events.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import ec.edu.ups.icc.proyectointegrador.events.entities.EventEntity;

public interface EventRepository extends JpaRepository<EventEntity, Long>, JpaSpecificationExecutor<EventEntity> {

    @EntityGraph(attributePaths = {"organizer","category"})
    Optional<EventEntity> findByIdAndDeletedFalse(Long id);

    @EntityGraph(attributePaths = {"organizer","category"})
    Optional<EventEntity> findByIdAndOrganizerIdAndDeletedFalse(Long id,Long organizerId);
}
