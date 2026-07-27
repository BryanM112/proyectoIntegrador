package ec.edu.ups.icc.proyectointegrador.audit.services.impl;

import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.ups.icc.proyectointegrador.audit.entities.AuditLogEntity;
import ec.edu.ups.icc.proyectointegrador.audit.enums.AuditResult;
import ec.edu.ups.icc.proyectointegrador.audit.repositories.AuditLogRepository;
import ec.edu.ups.icc.proyectointegrador.audit.services.AuditService;

@Service
public class AuditServiceImpl implements AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditServiceImpl.class);

    private final AuditLogRepository repository;

    public AuditServiceImpl(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(
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
    ) {
        try {
            AuditLogEntity entity = new AuditLogEntity();
            entity.setActorId(actorId);
            entity.setAction(action);
            entity.setResourceType(resourceType);
            entity.setResourceId(resourceId);
            entity.setNewValue(newValue);
            entity.setResult(result);
            entity.setIpAddress(ipAddress);
            entity.setHttpMethod(httpMethod);
            entity.setEndpoint(endpoint);
            entity.setCorrelationId(correlationId);
            entity.setCreatedAt(OffsetDateTime.now());

            repository.save(entity);
        } catch (Exception ex) {
            // La auditoría nunca debe tumbar la petición real del usuario.
            logger.error("No se pudo registrar el log de auditoría", ex);
        }
    }
}