package ec.edu.ups.icc.proyectointegrador.audit.services;

import ec.edu.ups.icc.proyectointegrador.audit.enums.AuditResult;

public interface AuditService {
    void record(
            Long actorId,
            String action,
            String resourceType,
            Long resourceId,
            String newValue,
            AuditResult result,
            String ipAddress,
            String httpMethod,
            String endpoint,
            String correlationId
    );
}