package ec.edu.ups.icc.proyectointegrador.registrations.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import ec.edu.ups.icc.proyectointegrador.registrations.entities.RegistrationEntity;
import ec.edu.ups.icc.proyectointegrador.registrations.enums.RegistrationStatus;

public interface RegistrationRepository
        extends JpaRepository<RegistrationEntity, Long> {

    boolean existsByEventIdAndParticipantId(
            Long eventId,
            Long participantId
    );

    @EntityGraph(attributePaths = {"event", "participant"})
    Optional<RegistrationEntity> findWithRelationsById(Long id);

    @EntityGraph(attributePaths = {"event", "participant"})
    Optional<RegistrationEntity> findWithRelationsByRegistrationCode(
            UUID registrationCode
    );

    @EntityGraph(attributePaths = {"event", "participant"})
    List<RegistrationEntity> findAllByParticipantIdOrderByRegisteredAtDesc(
            Long participantId
    );

    @EntityGraph(attributePaths = {"event", "participant"})
    List<RegistrationEntity> findAllByEventIdOrderByRegisteredAtDesc(
            Long eventId
    );

    @EntityGraph(attributePaths = {"event", "participant"})
    List<RegistrationEntity> findAllByEventIdAndStatusOrderByRegisteredAtDesc(
            Long eventId,
            RegistrationStatus status
    );
}