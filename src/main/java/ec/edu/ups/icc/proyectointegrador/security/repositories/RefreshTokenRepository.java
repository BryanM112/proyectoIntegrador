package ec.edu.ups.icc.proyectointegrador.security.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import ec.edu.ups.icc.proyectointegrador.security.entities.RefreshTokenEntity;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshTokenEntity, Long> {@EntityGraph(attributePaths = {"user", "user.roles"})
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    Optional<RefreshTokenEntity> findByTokenId(UUID tokenId);

    boolean existsByTokenHash(String tokenHash);
}
