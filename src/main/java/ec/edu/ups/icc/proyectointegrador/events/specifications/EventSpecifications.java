package ec.edu.ups.icc.proyectointegrador.events.specifications;

import java.time.OffsetDateTime;

import org.springframework.data.jpa.domain.Specification;

import ec.edu.ups.icc.proyectointegrador.events.entities.EventEntity;
import ec.edu.ups.icc.proyectointegrador.events.enums.EventModality;
import ec.edu.ups.icc.proyectointegrador.events.enums.EventStatus;
import jakarta.persistence.criteria.JoinType;

public final class EventSpecifications {

    private EventSpecifications() {
    }

    public static Specification<EventEntity> notDeleted() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isFalse(
                        root.get("deleted")
                );
    }

    public static Specification<EventEntity> hasSearch(
            String search
    ) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String pattern =
                    "%" + search.trim().toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get("title")
                            ),
                            pattern
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get("description")
                            ),
                            pattern
                    )
            );
        };
    }

    public static Specification<EventEntity> hasStatus(
            EventStatus status
    ) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("status"),
                    status
            );
        };
    }

    public static Specification<EventEntity> hasModality(
            EventModality modality
    ) {
        return (root, query, criteriaBuilder) -> {
            if (modality == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("modality"),
                    modality
            );
        };
    }

    public static Specification<EventEntity> hasCategoryId(
            Long categoryId
    ) {
        return (root, query, criteriaBuilder) -> {
            if (categoryId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.join("category", JoinType.INNER)
                            .get("id"),
                    categoryId
            );
        };
    }

    public static Specification<EventEntity> hasOrganizerId(
            Long organizerId
    ) {
        return (root, query, criteriaBuilder) -> {
            if (organizerId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.join("organizer", JoinType.INNER)
                            .get("id"),
                    organizerId
            );
        };
    }

    public static Specification<EventEntity> startsFrom(
            OffsetDateTime startFrom
    ) {
        return (root, query, criteriaBuilder) -> {
            if (startFrom == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get("startAt"),
                    startFrom
            );
        };
    }

    public static Specification<EventEntity> startsUntil(
            OffsetDateTime startTo
    ) {
        return (root, query, criteriaBuilder) -> {
            if (startTo == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.lessThanOrEqualTo(
                    root.get("startAt"),
                    startTo
            );
        };
    }


    public static Specification<EventEntity> visibleToUser(
        boolean isAdmin,
        boolean isOrganizer,
        Long userId
    ) {
    return (root, query, criteriaBuilder) -> {
        if (isAdmin) {
            return criteriaBuilder.conjunction();
        }

        if (isOrganizer) {
            return criteriaBuilder.or(
                    criteriaBuilder.equal(
                            root.get("status"),
                            EventStatus.PUBLISHED
                    ),
                    criteriaBuilder.and(
                            criteriaBuilder.equal(
                                    root.get("organizer").get("id"),
                                    userId
                            ),
                            root.get("status").in(
                                    EventStatus.DRAFT,
                                    EventStatus.CANCELLED,
                                    EventStatus.FINISHED
                            )
                    )
            );
        }

        return criteriaBuilder.equal(
                root.get("status"),
                EventStatus.PUBLISHED
        );
    };
    }
}
