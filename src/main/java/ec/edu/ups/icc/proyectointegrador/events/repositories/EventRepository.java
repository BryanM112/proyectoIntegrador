package ec.edu.ups.icc.proyectointegrador.events.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ec.edu.ups.icc.proyectointegrador.events.entities.EventEntity;
import jakarta.persistence.LockModeType;

public interface EventRepository extends JpaRepository<EventEntity, Long>, JpaSpecificationExecutor<EventEntity> {

    @EntityGraph(attributePaths = {"organizer","category"})
    Optional<EventEntity> findByIdAndDeletedFalse(Long id);

    @EntityGraph(attributePaths = {"organizer","category"})
    Optional<EventEntity> findByIdAndOrganizerIdAndDeletedFalse(Long id,Long organizerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT event
        FROM EventEntity event
        WHERE event.id = :id
          AND event.deleted = false
        """)
    Optional<EventEntity> findByIdAndDeletedFalseForUpdate(
        @Param("id") Long id
    );
}
