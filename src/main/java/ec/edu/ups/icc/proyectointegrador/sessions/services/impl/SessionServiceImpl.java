package ec.edu.ups.icc.proyectointegrador.sessions.services.impl;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.ups.icc.proyectointegrador.core.exceptions.BusinessRuleException;
import ec.edu.ups.icc.proyectointegrador.core.exceptions.ConflictException;
import ec.edu.ups.icc.proyectointegrador.core.exceptions.ResourceNotFoundException;
import ec.edu.ups.icc.proyectointegrador.events.entities.EventEntity;
import ec.edu.ups.icc.proyectointegrador.events.enums.EventStatus;
import ec.edu.ups.icc.proyectointegrador.events.repositories.EventRepository;
import ec.edu.ups.icc.proyectointegrador.roles.enums.RoleName;
import ec.edu.ups.icc.proyectointegrador.sessions.dtos.CreateSessionDto;
import ec.edu.ups.icc.proyectointegrador.sessions.dtos.SessionResponseDto;
import ec.edu.ups.icc.proyectointegrador.sessions.dtos.UpdateSessionDto;
import ec.edu.ups.icc.proyectointegrador.sessions.entities.SessionEntity;
import ec.edu.ups.icc.proyectointegrador.sessions.mappers.SessionMapper;
import ec.edu.ups.icc.proyectointegrador.sessions.repositories.SessionRepository;
import ec.edu.ups.icc.proyectointegrador.sessions.services.SessionService;
import ec.edu.ups.icc.proyectointegrador.users.entities.UserEntity;
import ec.edu.ups.icc.proyectointegrador.users.repositories.UserRepository;

@Service
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final SessionMapper mapper;

    public SessionServiceImpl(
            SessionRepository sessionRepository,
            EventRepository eventRepository,
            UserRepository userRepository,
            SessionMapper mapper
    ) {
        this.sessionRepository = sessionRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponseDto> findByEvent(
            Long eventId,
            String authenticatedEmail
    ) {
        EventEntity event = findEvent(eventId);
        UserEntity authenticatedUser =
                findAuthenticatedUser(authenticatedEmail);

        validateEventVisibility(event, authenticatedUser);

        return sessionRepository
                .findAllByEventIdOrderByStartAtAsc(eventId)
                .stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SessionResponseDto findById(
            Long id,
            String authenticatedEmail
    ) {
        SessionEntity session = sessionRepository
                .findWithEventById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Sesión no encontrada"
                        )
                );

        UserEntity authenticatedUser =
                findAuthenticatedUser(authenticatedEmail);

        validateEventVisibility(
                session.getEvent(),
                authenticatedUser
        );

        return mapper.toResponseDto(session);
    }

    @Override
    @Transactional
    public SessionResponseDto create(
            CreateSessionDto dto,
            String authenticatedEmail
    ) {
        EventEntity event = findEvent(dto.eventId());

        UserEntity authenticatedUser =
                findAuthenticatedUser(authenticatedEmail);

        validateEventOwnership(event, authenticatedUser);

        String normalizedTitle =
                normalizeRequiredText(dto.title());

        String normalizedDescription =
                normalizeOptionalText(dto.description());

        String normalizedLocation =
                normalizeOptionalText(dto.location());

        String normalizedVirtualUrl =
                normalizeOptionalText(dto.virtualUrl());

        validateSessionDates(
                dto.startAt(),
                dto.endAt()
        );

        validateSessionInsideEvent(
                event,
                dto.startAt(),
                dto.endAt()
        );

        validateDuplicateForCreate(
                event.getId(),
                normalizedTitle,
                dto.startAt()
        );

        validateOverlapForCreate(
                event.getId(),
                dto.startAt(),
                dto.endAt()
        );

        SessionEntity session = mapper.toEntity(dto);

        session.setEvent(event);
        session.setTitle(normalizedTitle);
        session.setDescription(normalizedDescription);
        session.setLocation(normalizedLocation);
        session.setVirtualUrl(normalizedVirtualUrl);

        SessionEntity savedSession =
        sessionRepository.save(session);

        sessionRepository.flush();

        SessionEntity persistedSession = sessionRepository
            .findWithEventById(savedSession.getId())
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Sesión creada, pero no pudo recuperarse"
                    )
            );

        return mapper.toResponseDto(persistedSession);
    }

    @Override
    @Transactional
    public SessionResponseDto update(
            Long id,
            UpdateSessionDto dto,
            String authenticatedEmail
    ) {
        SessionEntity session = sessionRepository
                .findWithEventById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Sesión no encontrada"
                        )
                );

        EventEntity event = session.getEvent();

        UserEntity authenticatedUser =
                findAuthenticatedUser(authenticatedEmail);

        validateEventOwnership(event, authenticatedUser);

        String normalizedTitle =
                normalizeRequiredText(dto.title());

        String normalizedDescription =
                normalizeOptionalText(dto.description());

        String normalizedLocation =
                normalizeOptionalText(dto.location());

        String normalizedVirtualUrl =
                normalizeOptionalText(dto.virtualUrl());

        validateSessionDates(
                dto.startAt(),
                dto.endAt()
        );

        validateSessionInsideEvent(
                event,
                dto.startAt(),
                dto.endAt()
        );

        validateDuplicateForUpdate(
                event.getId(),
                normalizedTitle,
                dto.startAt(),
                session.getId()
        );

        validateOverlapForUpdate(
                event.getId(),
                dto.startAt(),
                dto.endAt(),
                session.getId()
        );

        mapper.updateEntity(session, dto);

        session.setTitle(normalizedTitle);
        session.setDescription(normalizedDescription);
        session.setLocation(normalizedLocation);
        session.setVirtualUrl(normalizedVirtualUrl);

        SessionEntity updatedSession =
        sessionRepository.save(session);

        sessionRepository.flush();

        SessionEntity persistedSession = sessionRepository
            .findWithEventById(updatedSession.getId())
            .orElseThrow(() ->
                new ResourceNotFoundException(
                        "Sesión actualizada, pero no pudo recuperarse"
                )
            );

        return mapper.toResponseDto(persistedSession);
    }

    @Override
    @Transactional
    public void delete(
            Long id,
            String authenticatedEmail
    ) {
        SessionEntity session = sessionRepository
                .findWithEventById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Sesión no encontrada"
                        )
                );

        UserEntity authenticatedUser =
                findAuthenticatedUser(authenticatedEmail);

        validateEventOwnership(
                session.getEvent(),
                authenticatedUser
        );

        sessionRepository.delete(session);
    }

    private EventEntity findEvent(Long eventId) {
        return eventRepository
                .findByIdAndDeletedFalse(eventId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Evento no encontrado"
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

    private void validateEventOwnership(
            EventEntity event,
            UserEntity authenticatedUser
    ) {
        boolean isAdmin = hasRole(
                authenticatedUser,
                RoleName.ADMIN
        );

        boolean isOwner = event.getOrganizer()
                .getId()
                .equals(authenticatedUser.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(
                    "No tiene permisos para modificar las sesiones de este evento"
            );
        }
    }

    private void validateEventVisibility(
            EventEntity event,
            UserEntity authenticatedUser
    ) {
        boolean isAdmin = hasRole(
                authenticatedUser,
                RoleName.ADMIN
        );

        if (isAdmin) {
            return;
        }

        boolean isOrganizer = hasRole(
                authenticatedUser,
                RoleName.ORGANIZER
        );

        boolean isOwner = event.getOrganizer()
                .getId()
                .equals(authenticatedUser.getId());

        boolean isPublished =
                event.getStatus() == EventStatus.PUBLISHED;

        if (isPublished) {
            return;
        }

        if (isOrganizer && isOwner) {
            return;
        }

        /*
         * Se devuelve 404 para no revelar la existencia de sesiones
         * pertenecientes a un evento no visible para el usuario.
         */
        throw new ResourceNotFoundException(
                "Evento no encontrado"
        );
    }

    private void validateSessionDates(
            OffsetDateTime startAt,
            OffsetDateTime endAt
    ) {
        if (!startAt.isBefore(endAt)) {
            throw new BusinessRuleException(
                    "El inicio de la sesión debe ser anterior a su finalización"
            );
        }
    }

    private void validateSessionInsideEvent(
            EventEntity event,
            OffsetDateTime startAt,
            OffsetDateTime endAt
    ) {
        boolean startsBeforeEvent =
                startAt.isBefore(event.getStartAt());

        boolean endsAfterEvent =
                endAt.isAfter(event.getEndAt());

        if (startsBeforeEvent || endsAfterEvent) {
            throw new BusinessRuleException(
                    "La sesión debe desarrollarse dentro del horario del evento"
            );
        }
    }

    private void validateDuplicateForCreate(
            Long eventId,
            String title,
            OffsetDateTime startAt
    ) {
        boolean duplicate =
                sessionRepository
                        .existsByEventIdAndTitleIgnoreCaseAndStartAt(
                                eventId,
                                title,
                                startAt
                        );

        if (duplicate) {
            throw new ConflictException(
                    "Ya existe una sesión con el mismo título y hora de inicio en este evento"
            );
        }
    }

    private void validateDuplicateForUpdate(
            Long eventId,
            String title,
            OffsetDateTime startAt,
            Long sessionId
    ) {
        boolean duplicate =
                sessionRepository
                        .existsByEventIdAndTitleIgnoreCaseAndStartAtAndIdNot(
                                eventId,
                                title,
                                startAt,
                                sessionId
                        );

        if (duplicate) {
            throw new ConflictException(
                    "Ya existe una sesión con el mismo título y hora de inicio en este evento"
            );
        }
    }

    private void validateOverlapForCreate(
            Long eventId,
            OffsetDateTime startAt,
            OffsetDateTime endAt
    ) {
        boolean overlaps =
                sessionRepository
                        .existsByEventIdAndStartAtLessThanAndEndAtGreaterThan(
                                eventId,
                                endAt,
                                startAt
                        );

        if (overlaps) {
            throw new ConflictException(
                    "La sesión se superpone con otra sesión del evento"
            );
        }
    }

    private void validateOverlapForUpdate(
            Long eventId,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            Long sessionId
    ) {
        boolean overlaps =
                sessionRepository
                        .existsByEventIdAndStartAtLessThanAndEndAtGreaterThanAndIdNot(
                                eventId,
                                endAt,
                                startAt,
                                sessionId
                        );

        if (overlaps) {
            throw new ConflictException(
                    "La sesión se superpone con otra sesión del evento"
            );
        }
    }

    private String normalizeRequiredText(String value) {
        return value.trim();
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isBlank()
                ? null
                : normalized;
    }
}