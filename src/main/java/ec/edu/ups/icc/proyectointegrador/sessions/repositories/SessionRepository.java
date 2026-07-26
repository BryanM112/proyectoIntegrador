package ec.edu.ups.icc.proyectointegrador.sessions.repositories;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import ec.edu.ups.icc.proyectointegrador.sessions.entities.SessionEntity;

public interface SessionRepository extends JpaRepository<SessionEntity, Long> {

    @EntityGraph(attributePaths = "event")
    Optional<SessionEntity> findWithEventById(Long id);

    @EntityGraph(attributePaths = "event")
    List<SessionEntity> findAllByEventIdOrderByStartAtAsc(Long eventId);

    boolean existsByEventIdAndTitleIgnoreCaseAndStartAt(
            Long eventId,
            String title,
            OffsetDateTime startAt
    );

    boolean existsByEventIdAndTitleIgnoreCaseAndStartAtAndIdNot(
            Long eventId,
            String title,
            OffsetDateTime startAt,
            Long id
    );

    boolean existsByEventIdAndStartAtLessThanAndEndAtGreaterThan(
            Long eventId,
            OffsetDateTime endAt,
            OffsetDateTime startAt
    );

    boolean existsByEventIdAndStartAtLessThanAndEndAtGreaterThanAndIdNot(
            Long eventId,
            OffsetDateTime endAt,
            OffsetDateTime startAt,
            Long id
    );
}