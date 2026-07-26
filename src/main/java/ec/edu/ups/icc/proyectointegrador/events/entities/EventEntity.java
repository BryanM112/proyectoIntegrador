package ec.edu.ups.icc.proyectointegrador.events.entities;

import java.time.OffsetDateTime;

import ec.edu.ups.icc.proyectointegrador.categories.entities.CategoryEntity;
import ec.edu.ups.icc.proyectointegrador.events.enums.EventModality;
import ec.edu.ups.icc.proyectointegrador.events.enums.EventStatus;
import ec.edu.ups.icc.proyectointegrador.users.entities.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "events")
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "title",
            nullable = false,
            length = 160
    )
    private String title;

    @Column(
            name = "description",
            nullable = false,
            columnDefinition = "text"
    )
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "modality",
            nullable = false,
            length = 20
    )
    private EventModality modality;

    @Column(
            name = "location",
            length = 200
    )
    private String location;

    @Column(
            name = "virtual_url",
            length = 500
    )
    private String virtualUrl;

    @Column(
            name = "capacity",
            nullable = false
    )
    private Integer capacity;

    @Column(
            name = "available_capacity",
            nullable = false
    )
    private Integer availableCapacity;

    @Column(
            name = "registration_start_at",
            nullable = false
    )
    private OffsetDateTime registrationStartAt;

    @Column(
            name = "registration_end_at",
            nullable = false
    )
    private OffsetDateTime registrationEndAt;

    @Column(
            name = "start_at",
            nullable = false
    )
    private OffsetDateTime startAt;

    @Column(
            name = "end_at",
            nullable = false
    )
    private OffsetDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private EventStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "organizer_id",
            nullable = false
    )
    private UserEntity organizer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "category_id",
            nullable = false
    )
    private CategoryEntity category;

    @Column(
            name = "deleted",
            nullable = false
    )
    private Boolean deleted;

    @Version
    @Column(
            name = "version",
            nullable = false
    )
    private Long version;

    @Column(
            name = "created_at",
            nullable = false
    )
    private OffsetDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private OffsetDateTime updatedAt;

    public EventEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EventModality getModality() {
        return modality;
    }

    public void setModality(EventModality modality) {
        this.modality = modality;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getVirtualUrl() {
        return virtualUrl;
    }

    public void setVirtualUrl(String virtualUrl) {
        this.virtualUrl = virtualUrl;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getAvailableCapacity() {
        return availableCapacity;
    }

    public void setAvailableCapacity(Integer availableCapacity) {
        this.availableCapacity = availableCapacity;
    }

    public OffsetDateTime getRegistrationStartAt() {
        return registrationStartAt;
    }

    public void setRegistrationStartAt(
            OffsetDateTime registrationStartAt
    ) {
        this.registrationStartAt = registrationStartAt;
    }

    public OffsetDateTime getRegistrationEndAt() {
        return registrationEndAt;
    }

    public void setRegistrationEndAt(
            OffsetDateTime registrationEndAt
    ) {
        this.registrationEndAt = registrationEndAt;
    }

    public OffsetDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(OffsetDateTime startAt) {
        this.startAt = startAt;
    }

    public OffsetDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(OffsetDateTime endAt) {
        this.endAt = endAt;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public UserEntity getOrganizer() {
        return organizer;
    }

    public void setOrganizer(UserEntity organizer) {
        this.organizer = organizer;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
