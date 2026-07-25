package ec.edu.ups.icc.proyectointegrador.security.services;

import ec.edu.ups.icc.proyectointegrador.security.dtos.AuthResponseDto;
import ec.edu.ups.icc.proyectointegrador.security.dtos.LoginRequestDto;
import ec.edu.ups.icc.proyectointegrador.security.dtos.RefreshRequestDto;
import ec.edu.ups.icc.proyectointegrador.security.dtos.RegisterRequestDto;
import ec.edu.ups.icc.proyectointegrador.users.dtos.UserResponseDto;

public interface AuthService {
    AuthResponseDto login(LoginRequestDto request, String clienteIP);

    AuthResponseDto refresh(RefreshRequestDto request, String clientIp);

    UserResponseDto register(RegisterRequestDto request);

    UserResponseDto findAuthenticatedUser(String email);

    void logout(RefreshRequestDto request);
}
