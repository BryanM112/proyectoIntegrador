package ec.edu.ups.icc.proyectointegrador.security.services.impl;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Locale;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.ups.icc.proyectointegrador.core.exceptions.BusinessRuleException;
import ec.edu.ups.icc.proyectointegrador.core.exceptions.ConflictException;
import ec.edu.ups.icc.proyectointegrador.core.exceptions.ResourceNotFoundException;
import ec.edu.ups.icc.proyectointegrador.roles.entities.RoleEntity;
import ec.edu.ups.icc.proyectointegrador.roles.enums.RoleName;
import ec.edu.ups.icc.proyectointegrador.roles.repositories.RoleRepository;
import ec.edu.ups.icc.proyectointegrador.security.dtos.AuthResponseDto;
import ec.edu.ups.icc.proyectointegrador.security.dtos.CreatedRefreshTokenDto;
import ec.edu.ups.icc.proyectointegrador.security.dtos.LoginRequestDto;
import ec.edu.ups.icc.proyectointegrador.security.dtos.RefreshRequestDto;
import ec.edu.ups.icc.proyectointegrador.security.dtos.RegisterRequestDto;
import ec.edu.ups.icc.proyectointegrador.security.dtos.RotatedRefreshTokenDto;
import ec.edu.ups.icc.proyectointegrador.security.services.AuthService;
import ec.edu.ups.icc.proyectointegrador.security.services.CustomUserDetailsService;
import ec.edu.ups.icc.proyectointegrador.security.services.JwtService;
import ec.edu.ups.icc.proyectointegrador.security.services.RefreshTokenService;
import ec.edu.ups.icc.proyectointegrador.users.dtos.UserResponseDto;
import ec.edu.ups.icc.proyectointegrador.users.entities.UserEntity;
import ec.edu.ups.icc.proyectointegrador.users.enums.UserStatus;
import ec.edu.ups.icc.proyectointegrador.users.mappers.UserMapper;
import ec.edu.ups.icc.proyectointegrador.users.repositories.UserRepository;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final CustomUserDetailsService userDetailsService;

    

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtService jwtService,
            RefreshTokenService refreshTokenService, UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, UserMapper userMapper, CustomUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.userDetailsService = userDetailsService;
    }

    @Override
    @Transactional
    public AuthResponseDto login(
            LoginRequestDto request,
            String clientIp
    ) {
        String normalizedEmail = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                normalizedEmail,
                                request.password()
                        )
                );

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        UserEntity user = userRepository
                .findWithRolesByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Credenciales inválidas"
                        )
                );

        String accessToken =
                jwtService.generateAccessToken(userDetails);

        CreatedRefreshTokenDto refreshToken =
                refreshTokenService.create(
                        user,
                        clientIp
                );

        return new AuthResponseDto(
                accessToken,
                refreshToken.token(),
                "Bearer",
                jwtService.getAccessExpirationSeconds()
        );
    }

    @Override
    @Transactional
    public UserResponseDto register(RegisterRequestDto request) {
        String normalizedEmail = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException(
                    "Ya existe un usuario registrado con ese correo"
            );
        }

        RoleEntity participantRole = roleRepository
                .findByName(RoleName.PARTICIPANT)
                .orElseThrow(() ->
                        new BusinessRuleException(
                                "El rol PARTICIPANT no está configurado"
                        )
                );

        OffsetDateTime now =
                OffsetDateTime.now(ZoneOffset.UTC);

        UserEntity user = new UserEntity();
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        HashSet<RoleEntity> roles = new HashSet<>();
        roles.add(participantRole);
        user.setRoles(roles);

        UserEntity savedUser = userRepository.save(user);

        return userMapper.toResponseDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto findAuthenticatedUser(String email) {
        String normalizedEmail = email
                .trim()
                .toLowerCase(Locale.ROOT);

        return userRepository
                .findWithRolesByEmail(normalizedEmail)
                .map(userMapper::toResponseDto)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuario autenticado no encontrado"
                        )
                );
    }

    @Override
    @Transactional
    public AuthResponseDto refresh(RefreshRequestDto request, String clientIp) {
        RotatedRefreshTokenDto rotation = refreshTokenService.rotate(request.refreshToken(), clientIp);

        UserDetails userDetails = userDetailsService.loadUserByUsername(rotation.user().getEmail());

        String accessToken = jwtService.generateAccessToken(userDetails);

        return new AuthResponseDto(
            accessToken,
            rotation.refreshToken().token(),
            "Bearer",
            jwtService.getAccessExpirationSeconds()
        );
    }

    @Override
    public void logout(RefreshRequestDto request) {
        refreshTokenService.revoke(request.refreshToken());
    }
}