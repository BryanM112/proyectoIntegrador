package ec.edu.ups.icc.proyectointegrador.registrations.services.impl;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.ups.icc.proyectointegrador.core.exceptions.BusinessRuleException;
import ec.edu.ups.icc.proyectointegrador.core.exceptions.ConflictException;
import ec.edu.ups.icc.proyectointegrador.core.exceptions.ResourceNotFoundException;
import ec.edu.ups.icc.proyectointegrador.events.entities.EventEntity;
import ec.edu.ups.icc.proyectointegrador.events.enums.EventStatus;
import ec.edu.ups.icc.proyectointegrador.events.repositories.EventRepository;
import ec.edu.ups.icc.proyectointegrador.registrations.dtos.CancelRegistrationDto;
import ec.edu.ups.icc.proyectointegrador.registrations.dtos.CreateRegistrationDto;
import ec.edu.ups.icc.proyectointegrador.registrations.dtos.RegistrationResponseDto;
import ec.edu.ups.icc.proyectointegrador.registrations.dtos.UpdateRegistrationStatusDto;
import ec.edu.ups.icc.proyectointegrador.registrations.entities.RegistrationEntity;
import ec.edu.ups.icc.proyectointegrador.registrations.enums.RegistrationStatus;
import ec.edu.ups.icc.proyectointegrador.registrations.mappers.RegistrationMapper;
import ec.edu.ups.icc.proyectointegrador.registrations.repositories.RegistrationRepository;
import ec.edu.ups.icc.proyectointegrador.registrations.services.RegistrationService;
import ec.edu.ups.icc.proyectointegrador.roles.enums.RoleName;
import ec.edu.ups.icc.proyectointegrador.users.entities.UserEntity;
import ec.edu.ups.icc.proyectointegrador.users.enums.UserStatus;
import ec.edu.ups.icc.proyectointegrador.users.repositories.UserRepository;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RegistrationMapper mapper;

    public RegistrationServiceImpl(
            RegistrationRepository registrationRepository,
            EventRepository eventRepository,
            UserRepository userRepository,
            RegistrationMapper mapper
    ) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public RegistrationResponseDto findById(
            Long id,
            String authenticatedEmail
    ) {
        RegistrationEntity registration =
                findRegistration(id);

        UserEntity authenticatedUser =
                findAuthenticatedUser(authenticatedEmail);

        validateRegistrationVisibility(
                registration,
                authenticatedUser
        );

        return mapper.toResponseDto(registration);
    }

    @Override
    @Transactional(readOnly = true)
    public RegistrationResponseDto findByCode(
            UUID registrationCode,
            String authenticatedEmail
    ) {
        RegistrationEntity registration =
                registrationRepository
                        .findWithRelationsByRegistrationCode(
                                registrationCode
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Inscripción no encontrada"
                                )
                        );

        UserEntity authenticatedUser =
                findAuthenticatedUser(authenticatedEmail);

        validateRegistrationVisibility(
                registration,
                authenticatedUser
        );

        return mapper.toResponseDto(registration);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationResponseDto> findMyRegistrations(
            String authenticatedEmail
    ) {
        UserEntity authenticatedUser =
                findAuthenticatedUser(authenticatedEmail);

        return registrationRepository
                .findAllByParticipantIdOrderByRegisteredAtDesc(
                        authenticatedUser.getId()
                )
                .stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationResponseDto> findByEvent(
            Long eventId,
            RegistrationStatus status,
            String authenticatedEmail
    ) {
        EventEntity event = eventRepository
                .findByIdAndDeletedFalse(eventId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Evento no encontrado"
                        )
                );

        UserEntity authenticatedUser =
                findAuthenticatedUser(authenticatedEmail);

        validateEventManagementAccess(
                event,
                authenticatedUser
        );

        List<RegistrationEntity> registrations;

        if (status == null) {
            registrations = registrationRepository
                    .findAllByEventIdOrderByRegisteredAtDesc(
                            eventId
                    );
        } else {
            registrations = registrationRepository
                    .findAllByEventIdAndStatusOrderByRegisteredAtDesc(
                            eventId,
                            status
                    );
        }

        return registrations
                .stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    private RegistrationEntity findRegistration(Long id) {
        return registrationRepository
                .findWithRelationsById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Inscripción no encontrada"
                        )
                );
    }

    private UserEntity findAuthenticatedUser(
            String authenticatedEmail
    ) {
        String normalizedEmail = authenticatedEmail
                .trim()
                .toLowerCase(Locale.ROOT);

        return userRepository
                .findWithRolesByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuario autenticado no encontrado"
                        )
                );
    }

    private boolean hasRole(
            UserEntity user,
            RoleName roleName
    ) {
        return user.getRoles()
                .stream()
                .anyMatch(role ->
                        role.getName() == roleName
                );
    }

    private void validateRegistrationVisibility(
            RegistrationEntity registration,
            UserEntity authenticatedUser
    ) {
        boolean isAdmin = hasRole(
                authenticatedUser,
                RoleName.ADMIN
        );

        boolean isOwner = registration
                .getParticipant()
                .getId()
                .equals(authenticatedUser.getId());

        boolean isEventOrganizer = registration
                .getEvent()
                .getOrganizer()
                .getId()
                .equals(authenticatedUser.getId());

        if (isAdmin || isOwner || isEventOrganizer) {
            return;
        }

        throw new ResourceNotFoundException(
                "Inscripción no encontrada"
        );
    }

    private void validateEventManagementAccess(
            EventEntity event,
            UserEntity authenticatedUser
    ) {
        boolean isAdmin = hasRole(
                authenticatedUser,
                RoleName.ADMIN
        );

        boolean isOwner = event
                .getOrganizer()
                .getId()
                .equals(authenticatedUser.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(
                    "No tiene permisos para gestionar las inscripciones de este evento"
            );
        }
    }

@Override
@Transactional
public RegistrationResponseDto create(
        CreateRegistrationDto dto,
        String authenticatedEmail
) {
    UserEntity participant =
            findAuthenticatedUser(authenticatedEmail);

    if (participant.getStatus() != UserStatus.ACTIVE) {
        throw new BusinessRuleException(
                "El usuario no está activo"
        );
    }

    EventEntity event = eventRepository
            .findByIdAndDeletedFalseForUpdate(dto.eventId())
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Evento no encontrado"
                    )
            );

    validateEventForRegistration(event);

    boolean alreadyRegistered =
            registrationRepository
                    .existsByEventIdAndParticipantId(
                            event.getId(),
                            participant.getId()
                    );

    if (alreadyRegistered) {
        throw new ConflictException(
                "El participante ya tiene una inscripción en este evento"
        );
    }

    if (event.getAvailableCapacity() <= 0) {
        throw new BusinessRuleException(
                "No existen cupos disponibles para este evento"
        );
    }

    OffsetDateTime now =
            OffsetDateTime.now(ZoneOffset.UTC);

    RegistrationEntity registration =
            new RegistrationEntity();

    registration.setRegistrationCode(UUID.randomUUID());
    registration.setEvent(event);
    registration.setParticipant(participant);
    registration.setStatus(RegistrationStatus.PENDING);
    registration.setRegisteredAt(now);
    registration.setStatusUpdatedAt(now);
    registration.setConfirmedAt(null);
    registration.setCancelledAt(null);

    event.setAvailableCapacity(
            event.getAvailableCapacity() - 1
    );

    /*
     * La inscripción y la disminución del cupo se ejecutan
     * dentro de la misma transacción.
     */
    eventRepository.save(event);

    RegistrationEntity savedRegistration =
            registrationRepository.save(registration);

    return mapper.toResponseDto(savedRegistration);
}


private void validateEventForRegistration(
        EventEntity event
) {
    OffsetDateTime now =
            OffsetDateTime.now(ZoneOffset.UTC);

    if (event.getStatus() != EventStatus.PUBLISHED) {
        throw new BusinessRuleException(
                "Solo es posible inscribirse en eventos publicados"
        );
    }

    if (now.isBefore(event.getRegistrationStartAt())) {
        throw new BusinessRuleException(
                "El periodo de inscripciones todavía no ha comenzado"
        );
    }

    if (now.isAfter(event.getRegistrationEndAt())) {
        throw new BusinessRuleException(
                "El periodo de inscripciones ya finalizó"
        );
    }
}

@Override
@Transactional
public RegistrationResponseDto updateStatus(
        Long id,
        UpdateRegistrationStatusDto dto,
        String authenticatedEmail
) {
    RegistrationEntity registration =
            findRegistration(id);

    UserEntity authenticatedUser =
            findAuthenticatedUser(authenticatedEmail);

    validateEventManagementAccess(
            registration.getEvent(),
            authenticatedUser
    );

    validateVersion(
            registration,
            dto.version()
    );

    RegistrationStatus currentStatus =
            registration.getStatus();

    RegistrationStatus newStatus =
            dto.status();

    validateStatusTransition(
            currentStatus,
            newStatus
    );

    OffsetDateTime now =
            OffsetDateTime.now(ZoneOffset.UTC);

    if (newStatus == RegistrationStatus.CONFIRMED) {
        registration.setConfirmedAt(now);
        registration.setCancelledAt(null);
    }

    if (newStatus == RegistrationStatus.REJECTED) {
        restoreEventCapacity(
                registration.getEvent().getId()
        );

        registration.setConfirmedAt(null);
        registration.setCancelledAt(null);
    }

    registration.setStatus(newStatus);
    registration.setStatusUpdatedAt(now);

    RegistrationEntity updatedRegistration =
            registrationRepository.save(registration);

    return mapper.toResponseDto(updatedRegistration);
}

private void validateVersion(
        RegistrationEntity registration,
        Long requestedVersion
) {
    if (!registration.getVersion().equals(requestedVersion)) {
        throw new ConflictException(
                "La inscripción fue modificada por otro usuario. Actualice la información e intente nuevamente"
        );
    }
}

private void validateStatusTransition(
        RegistrationStatus currentStatus,
        RegistrationStatus newStatus
) {
    if (currentStatus == newStatus) {
        throw new BusinessRuleException(
                "La inscripción ya se encuentra en el estado solicitado"
        );
    }

    if (currentStatus != RegistrationStatus.PENDING) {
        throw new BusinessRuleException(
                "Solo las inscripciones pendientes pueden ser confirmadas o rechazadas"
        );
    }

    if (newStatus != RegistrationStatus.CONFIRMED
            && newStatus != RegistrationStatus.REJECTED) {
        throw new BusinessRuleException(
                "El estado solo puede cambiar a CONFIRMED o REJECTED"
        );
    }
}



private void restoreEventCapacity(Long eventId) {
    EventEntity event = eventRepository
            .findByIdAndDeletedFalseForUpdate(eventId)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Evento no encontrado"
                    )
            );

    if (event.getAvailableCapacity()
            < event.getCapacity()) {

        event.setAvailableCapacity(
                event.getAvailableCapacity() + 1
        );

        eventRepository.save(event);
    }
}



@Override
@Transactional
public RegistrationResponseDto cancel(
        Long id,
        CancelRegistrationDto dto,
        String authenticatedEmail
) {
    RegistrationEntity registration =
            findRegistration(id);

    UserEntity authenticatedUser =
            findAuthenticatedUser(authenticatedEmail);

    validateCancellationOwnership(
            registration,
            authenticatedUser
    );

    validateVersion(
            registration,
            dto.version()
    );

    validateCancellationStatus(
            registration.getStatus()
    );

    /*
     * Se bloquea el evento y se devuelve el cupo reservado
     * por la inscripción.
     */
    restoreEventCapacity(
            registration.getEvent().getId()
    );

    OffsetDateTime now =
            OffsetDateTime.now(ZoneOffset.UTC);

    registration.setStatus(
            RegistrationStatus.CANCELLED
    );

    registration.setCancelledAt(now);
    registration.setStatusUpdatedAt(now);

    RegistrationEntity cancelledRegistration =
            registrationRepository.save(registration);

    return mapper.toResponseDto(cancelledRegistration);
}


private void validateCancellationOwnership(
        RegistrationEntity registration,
        UserEntity authenticatedUser
) {
    boolean isAdmin = hasRole(
            authenticatedUser,
            RoleName.ADMIN
    );

    boolean isParticipantOwner = registration
            .getParticipant()
            .getId()
            .equals(authenticatedUser.getId());

    if (isAdmin || isParticipantOwner) {
        return;
    }

    throw new ResourceNotFoundException(
            "Inscripción no encontrada"
    );
}


private void validateCancellationStatus(
        RegistrationStatus currentStatus
) {
    if (currentStatus == RegistrationStatus.CANCELLED) {
        throw new BusinessRuleException(
                "La inscripción ya está cancelada"
        );
    }

    if (currentStatus == RegistrationStatus.REJECTED) {
        throw new BusinessRuleException(
                "Una inscripción rechazada no puede cancelarse"
        );
    }

    if (currentStatus != RegistrationStatus.PENDING
            && currentStatus != RegistrationStatus.CONFIRMED) {
        throw new BusinessRuleException(
                "La inscripción no puede cancelarse en su estado actual"
        );
    }
}




}