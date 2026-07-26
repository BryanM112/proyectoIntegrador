package ec.edu.ups.icc.proyectointegrador.registrations.entities;

import java.time.OffsetDateTime;
import java.util.UUID;

import ec.edu.ups.icc.proyectointegrador.events.entities.EventEntity;
import ec.edu.ups.icc.proyectointegrador.registrations.enums.RegistrationStatus;
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
@Table(name = "registrations")
public class RegistrationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "registration_code",
            nullable = false,
            unique = true
    )
    private UUID registrationCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "event_id",
            nullable = false
    )
    private EventEntity event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "participant_id",
            nullable = false
    )
    private UserEntity participant;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private RegistrationStatus status;

    @Column(
            name = "registered_at",
            nullable = false
    )
    private OffsetDateTime registeredAt;

    @Column(
            name = "status_updated_at",
            nullable = false
    )
    private OffsetDateTime statusUpdatedAt;

    @Column(
            name = "confirmed_at"
    )
    private OffsetDateTime confirmedAt;

    @Column(
            name = "cancelled_at"
    )
    private OffsetDateTime cancelledAt;

    @Version
    @Column(
            name = "version",
            nullable = false
    )
    private Long version;

    public RegistrationEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getRegistrationCode() {
        return registrationCode;
    }

    public void setRegistrationCode(UUID registrationCode) {
        this.registrationCode = registrationCode;
    }

    public EventEntity getEvent() {
        return event;
    }

    public void setEvent(EventEntity event) {
        this.event = event;
    }

    public UserEntity getParticipant() {
        return participant;
    }

    public void setParticipant(UserEntity participant) {
        this.participant = participant;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public OffsetDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(OffsetDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public OffsetDateTime getStatusUpdatedAt() {
        return statusUpdatedAt;
    }

    public void setStatusUpdatedAt(OffsetDateTime statusUpdatedAt) {
        this.statusUpdatedAt = statusUpdatedAt;
    }

    public OffsetDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(OffsetDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public OffsetDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(OffsetDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}