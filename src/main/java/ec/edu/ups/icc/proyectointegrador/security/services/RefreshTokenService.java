package ec.edu.ups.icc.proyectointegrador.security.services;

import ec.edu.ups.icc.proyectointegrador.security.dtos.CreatedRefreshTokenDto;
import ec.edu.ups.icc.proyectointegrador.security.dtos.RotatedRefreshTokenDto;
import ec.edu.ups.icc.proyectointegrador.security.entities.RefreshTokenEntity;
import ec.edu.ups.icc.proyectointegrador.users.entities.UserEntity;

public interface RefreshTokenService {

    CreatedRefreshTokenDto create(UserEntity user, String clientIp);

    RefreshTokenEntity validate(String rawToken);

    String hash(String rawToken);

    RotatedRefreshTokenDto rotate(String rawToken,String clientIp);

    void revoke(String rawToken);
}