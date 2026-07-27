package ec.edu.ups.icc.proyectointegrador.audit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import ec.edu.ups.icc.proyectointegrador.audit.entities.AuditLogEntity;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {
}