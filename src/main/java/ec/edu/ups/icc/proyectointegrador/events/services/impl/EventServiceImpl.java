package ec.edu.ups.icc.proyectointegrador.events.services.impl;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.ups.icc.proyectointegrador.categories.entities.CategoryEntity;
import ec.edu.ups.icc.proyectointegrador.categories.repositories.CategoryRepository;
import ec.edu.ups.icc.proyectointegrador.events.dtos.CreateEventDto;
import ec.edu.ups.icc.proyectointegrador.events.dtos.EventResponseDto;
import ec.edu.ups.icc.proyectointegrador.events.dtos.UpdateEventDto;
import ec.edu.ups.icc.proyectointegrador.events.dtos.UpdateEventStatusDto;
import ec.edu.ups.icc.proyectointegrador.events.entities.EventEntity;
import ec.edu.ups.icc.proyectointegrador.events.enums.EventModality;
import ec.edu.ups.icc.proyectointegrador.events.enums.EventStatus;
import ec.edu.ups.icc.proyectointegrador.events.mappers.EventMapper;
import ec.edu.ups.icc.proyectointegrador.events.repositories.EventRepository;
import ec.edu.ups.icc.proyectointegrador.events.services.EventService;
import ec.edu.ups.icc.proyectointegrador.events.specifications.EventSpecifications;
import ec.edu.ups.icc.proyectointegrador.roles.enums.RoleName;
import ec.edu.ups.icc.proyectointegrador.users.entities.UserEntity;
import ec.edu.ups.icc.proyectointegrador.users.enums.UserStatus;
import ec.edu.ups.icc.proyectointegrador.roles.enums.RoleName;
import ec.edu.ups.icc.proyectointegrador.users.repositories.UserRepository;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper mapper;

    public EventServiceImpl(
            EventRepository eventRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            EventMapper mapper
    ) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventResponseDto> findAll(
            String search,
            EventStatus status,
            EventModality modality,
            Long categoryId,
            Long organizerId,
            OffsetDateTime startFrom,
            OffsetDateTime startTo,
            Pageable pageable,
            String authenticatedEmail
    ) {
        validateDateFilter(startFrom, startTo);

        UserEntity authenticatedUser =
            findAuthenticatedUser(authenticatedEmail);

        boolean isAdmin = hasRole(
            authenticatedUser,
            RoleName.ADMIN
        );

        boolean isOrganizer = hasRole(
            authenticatedUser,
            RoleName.ORGANIZER
        );

        Specification<EventEntity> specification =
                EventSpecifications.notDeleted()
                        .and(EventSpecifications.visibleToUser(isAdmin, isOrganizer, authenticatedUser.getId()))
                        .and(EventSpecifications.hasSearch(search))
                        .and(EventSpecifications.hasStatus(status))
                        .and(EventSpecifications.hasModality(modality))
                        .and(EventSpecifications.hasCategoryId(categoryId))
                        .and(EventSpecifications.hasOrganizerId(organizerId))
                        .and(EventSpecifications.startsFrom(startFrom))
                        .and(EventSpecifications.startsUntil(startTo));

        return eventRepository
                .findAll(specification, pageable)
                .map(mapper::toResponseDto);
    }

    @Override
@Transactional(readOnly = true)
public EventResponseDto findById(
        Long id,
        String authenticatedEmail
) {
    EventEntity event = eventRepository
            .findByIdAndDeletedFalse(id)
            .orElseThrow(() ->
                    new IllegalStateException(
                            "Evento no encontrado"
                    )
            );

        UserEntity authenticatedUser =
            findAuthenticatedUser(authenticatedEmail);

            validateEventVisibility(
            event,
            authenticatedUser
        );

        return mapper.toResponseDto(event);
    }

    @Override
    @Transactional
    public EventResponseDto create(CreateEventDto dto, String authenticatedEmail) {
        String normalizedEmail = authenticatedEmail.trim().toLowerCase(Locale.ROOT);

        UserEntity organizer = userRepository
                .findWithRolesByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado"));

        if (organizer.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("El usuario organizador no está activo");
        }

        CategoryEntity category = categoryRepository.findByIdAndActiveTrue(dto.categoryId()).orElseThrow(() -> new IllegalStateException("Categoría no encontrada"));

        validateEventDates(dto.registrationStartAt(),dto.registrationEndAt(),dto.startAt(),dto.endAt());

        String normalizedTitle = normalizeRequiredText(dto.title());

        String normalizedDescription = normalizeRequiredText(dto.description());

        String normalizedLocation = normalizeOptionalText(dto.location());

        String normalizedVirtualUrl = normalizeOptionalText(dto.virtualUrl());

        validateModality(dto.modality(), normalizedLocation, normalizedVirtualUrl);

        EventEntity event = mapper.toEntity(dto);

        OffsetDateTime now =
                OffsetDateTime.now(ZoneOffset.UTC);

        event.setTitle(normalizedTitle);
        event.setDescription(normalizedDescription);
        event.setLocation(normalizedLocation);
        event.setVirtualUrl(normalizedVirtualUrl);

        event.setAvailableCapacity(dto.capacity());
        event.setStatus(EventStatus.DRAFT);
        event.setOrganizer(organizer);
        event.setCategory(category);
        event.setDeleted(false);
        event.setCreatedAt(now);
        event.setUpdatedAt(now);

        EventEntity savedEvent =
                eventRepository.save(event);

        return mapper.toResponseDto(savedEvent);
    }

    private void validateEventDates(
            OffsetDateTime registrationStartAt,
            OffsetDateTime registrationEndAt,
            OffsetDateTime startAt,
            OffsetDateTime endAt
    ) {
        if (!registrationStartAt.isBefore(registrationEndAt)) {
            throw new IllegalStateException(
                    "El inicio de inscripciones debe ser anterior al fin de inscripciones"
            );
        }

        if (registrationEndAt.isAfter(startAt)) {
            throw new IllegalStateException(
                    "Las inscripciones deben finalizar antes o al iniciar el evento"
            );
        }

        if (!startAt.isBefore(endAt)) {
            throw new IllegalStateException(
                    "El inicio del evento debe ser anterior a su finalización"
            );
        }
    }

    private void validateModality(
            EventModality modality,
            String location,
            String virtualUrl
    ) {
        switch (modality) {
            case PRESENTIAL -> {
                if (location == null) {
                    throw new IllegalStateException(
                            "La ubicación es obligatoria para un evento presencial"
                    );
                }

                if (virtualUrl != null) {
                    throw new IllegalStateException(
                            "Un evento presencial no debe incluir una URL virtual"
                    );
                }
            }

            case VIRTUAL -> {
                if (virtualUrl == null) {
                    throw new IllegalStateException(
                            "La URL virtual es obligatoria para un evento virtual"
                    );
                }

                if (location != null) {
                    throw new IllegalStateException(
                            "Un evento virtual no debe incluir una ubicación física"
                    );
                }
            }

            case HYBRID -> {
                if (location == null) {
                    throw new IllegalStateException(
                            "La ubicación es obligatoria para un evento híbrido"
                    );
                }

                if (virtualUrl == null) {
                    throw new IllegalStateException(
                            "La URL virtual es obligatoria para un evento híbrido"
                    );
                }
            }
        }
    }

    private void validateDateFilter(
            OffsetDateTime startFrom,
            OffsetDateTime startTo
    ) {
        if (startFrom != null
                && startTo != null
                && startFrom.isAfter(startTo)) {
            throw new IllegalStateException(
                    "La fecha inicial del filtro no puede ser posterior a la fecha final"
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

        return normalized.isEmpty()
                ? null
                : normalized;
    }

    @Override
    @Transactional
    public EventResponseDto update(Long id, UpdateEventDto dto, String authenticatedEmail) {
        EventEntity event = eventRepository
            .findByIdAndDeletedFalse(id)
            .orElseThrow(() ->
                    new IllegalStateException(
                            "Evento no encontrado"
                    )
            );

        String normalizedEmail = authenticatedEmail.trim().toLowerCase(Locale.ROOT);

        UserEntity authenticatedUser = userRepository
            .findWithRolesByEmail(normalizedEmail)
            .orElseThrow(() ->
                    new IllegalStateException(
                            "Usuario autenticado no encontrado"
                    )
            );

        validateEventOwnership(event, authenticatedUser);

        validateVersion(event, dto.version());

        CategoryEntity category = categoryRepository
                .findByIdAndActiveTrue(dto.categoryId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Categoría no encontrada"
                        )
                );

        validateEventDates(
                dto.registrationStartAt(),
                dto.registrationEndAt(),
                dto.startAt(),
                dto.endAt()
        );

        String normalizedTitle =
            normalizeRequiredText(dto.title());

        String normalizedDescription =
            normalizeRequiredText(dto.description());

        String normalizedLocation =
            normalizeOptionalText(dto.location());

        String normalizedVirtualUrl =
            normalizeOptionalText(dto.virtualUrl());

        validateModality(
            dto.modality(),
            normalizedLocation,
            normalizedVirtualUrl
        );

        int registeredParticipants =
            event.getCapacity()
            - event.getAvailableCapacity();

        if (dto.capacity() < registeredParticipants) {
            throw new IllegalStateException(
                "La capacidad no puede ser menor que el número de participantes inscritos"
            );
        }

        int newAvailableCapacity = dto.capacity() - registeredParticipants;

        mapper.updateEntity(event, dto);

        event.setTitle(normalizedTitle);
        event.setDescription(normalizedDescription);
        event.setLocation(normalizedLocation);
        event.setVirtualUrl(normalizedVirtualUrl);
        event.setCategory(category);
        event.setAvailableCapacity(newAvailableCapacity);
        event.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        EventEntity updatedEvent = eventRepository.save(event);

        return mapper.toResponseDto(updatedEvent);
    }


    private void validateEventOwnership(
        EventEntity event,
        UserEntity authenticatedUser
    ) {
        boolean isAdmin = authenticatedUser
            .getRoles()
            .stream()
            .anyMatch(role ->
                    role.getName() == RoleName.ADMIN
            );

        boolean isOwner = event
            .getOrganizer()
            .getId()
            .equals(authenticatedUser.getId());

        if (!isAdmin && !isOwner) {
        throw new IllegalStateException(
                "No tiene permisos para modificar este evento"
            );
        }
    }


    private void validateVersion(
        EventEntity event,
        Long requestedVersion
    ) {
        if (!event.getVersion().equals(requestedVersion)) {
            throw new IllegalStateException(
                "El evento fue modificado por otra operación. Actualice la información e inténtelo nuevamente"
            );
        }
    }

    @Override
    @Transactional
    public void delete(Long id, String authenticatedEmail) {
        EventEntity event = eventRepository
            .findByIdAndDeletedFalse(id)
            .orElseThrow(() ->
                    new IllegalStateException(
                            "Evento no encontrado"
                    )
            );

        String normalizedEmail = authenticatedEmail
            .trim()
            .toLowerCase(Locale.ROOT);

        UserEntity authenticatedUser = userRepository
            .findWithRolesByEmail(normalizedEmail)
            .orElseThrow(() ->
                    new IllegalStateException(
                            "Usuario autenticado no encontrado"
                    )
            );

        validateEventOwnership(
            event,
            authenticatedUser
        );

        event.setDeleted(true);
        event.setUpdatedAt(
            OffsetDateTime.now(ZoneOffset.UTC)
        );

        eventRepository.save(event);
        }

    @Override
    @Transactional
    public EventResponseDto updateStatus(Long id, UpdateEventStatusDto dto, String authenticatedEmail) {
            EventEntity event = eventRepository
            .findByIdAndDeletedFalse(id)
            .orElseThrow(() ->
                    new IllegalStateException(
                            "Evento no encontrado"
                    )
            );

    String normalizedEmail = authenticatedEmail
            .trim()
            .toLowerCase(Locale.ROOT);

    UserEntity authenticatedUser = userRepository
            .findWithRolesByEmail(normalizedEmail)
            .orElseThrow(() ->
                    new IllegalStateException(
                            "Usuario autenticado no encontrado"
                    )
            );

    validateEventOwnership(
            event,
            authenticatedUser
    );

    validateVersion(
            event,
            dto.version()
    );

    OffsetDateTime now =
            OffsetDateTime.now(ZoneOffset.UTC);

    validateStatusTransition(
            event,
            dto.status(),
            now
    );

    event.setStatus(dto.status());
    event.setUpdatedAt(now);

    EventEntity updatedEvent =
            eventRepository.save(event);

    return mapper.toResponseDto(updatedEvent);
    }



    private void validateStatusTransition(
        EventEntity event,
        EventStatus requestedStatus,
        OffsetDateTime now
) {
    EventStatus currentStatus = event.getStatus();

    if (currentStatus == requestedStatus) {
        throw new IllegalStateException(
                "El evento ya tiene el estado solicitado"
        );
    }

    switch (currentStatus) {
        case DRAFT -> validateDraftTransition(
                event,
                requestedStatus,
                now
        );

        case PUBLISHED -> validatePublishedTransition(
                event,
                requestedStatus,
                now
        );

        case FINISHED -> throw new IllegalStateException(
                "Un evento finalizado no puede cambiar de estado"
        );

        case CANCELLED -> throw new IllegalStateException(
                "Un evento cancelado no puede cambiar de estado"
        );
    }
    }




    private void validateDraftTransition(
        EventEntity event,
        EventStatus requestedStatus,
        OffsetDateTime now
) {
    switch (requestedStatus) {
        case PUBLISHED -> validateEventCanBePublished(
                event,
                now
        );

        case CANCELLED -> {
            // Transición permitida.
        }

        default -> throw new IllegalStateException(
                "Un evento en borrador solo puede publicarse o cancelarse"
        );
    }
}




private void validatePublishedTransition(
        EventEntity event,
        EventStatus requestedStatus,
        OffsetDateTime now
) {
    switch (requestedStatus) {
        case FINISHED -> {
            if (now.isBefore(event.getEndAt())) {
                throw new IllegalStateException(
                        "El evento no puede finalizar antes de su fecha de finalización"
                );
            }
        }

        case CANCELLED -> {
            // Transición permitida.
        }

        default -> throw new IllegalStateException(
                "Un evento publicado solo puede finalizarse o cancelarse"
        );
    }
}



private void validateEventCanBePublished(
        EventEntity event,
        OffsetDateTime now
) {
    if (!event.getStartAt().isAfter(now)) {
        throw new IllegalStateException(
                "No se puede publicar un evento que ya comenzó"
        );
    }

    if (!event.getRegistrationEndAt().isAfter(now)) {
        throw new IllegalStateException(
                "No se puede publicar un evento cuyo periodo de inscripciones finalizó"
        );
    }

    if (!Boolean.TRUE.equals(
            event.getCategory().getActive()
    )) {
        throw new IllegalStateException(
                "No se puede publicar un evento con una categoría inactiva"
        );
    }

    if (event.getCapacity() <= 0) {
        throw new IllegalStateException(
                "El evento debe tener una capacidad válida"
            );
        }
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
                    new IllegalStateException(
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

        boolean isOwner = event
            .getOrganizer()
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

        throw new IllegalStateException(
            "Evento no encontrado"
        );
    }

}