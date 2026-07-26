package ec.edu.ups.icc.proyectointegrador.security.cotrollers;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.ups.icc.proyectointegrador.core.config.OpenApiConfig;
import ec.edu.ups.icc.proyectointegrador.security.dtos.AuthResponseDto;
import ec.edu.ups.icc.proyectointegrador.security.dtos.LoginRequestDto;
import ec.edu.ups.icc.proyectointegrador.security.dtos.RefreshRequestDto;
import ec.edu.ups.icc.proyectointegrador.security.dtos.RegisterRequestDto;
import ec.edu.ups.icc.proyectointegrador.security.services.AuthService;
import ec.edu.ups.icc.proyectointegrador.users.dtos.UserResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
@Tag(
        name = "Autenticación",
        description = """
                Registro, inicio de sesión, renovación de tokens,
                cierre de sesión y consulta del usuario autenticado
                """
)
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @Operation(
            summary = "Iniciar sesión",
            description = """
                    Autentica a un usuario mediante su correo electrónico
                    y contraseña.

                    Cuando las credenciales son correctas, devuelve un token
                    de acceso JWT y un token de actualización.

                    Este endpoint no requiere autenticación previa.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Inicio de sesión correcto",
                    content = @Content(
                            schema = @Schema(
                                    implementation = AuthResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Los datos enviados son inválidos"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Las credenciales son incorrectas"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario está bloqueado o inactivo"
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Se superó el límite de intentos de inicio de sesión"
            )
    })
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @Valid
            @RequestBody
            LoginRequestDto request,

            HttpServletRequest httpRequest
    ) {
        String clientIp = extractClientIp(httpRequest);

        return ResponseEntity.ok(
                service.login(request, clientIp)
        );
    }

    @Operation(
            summary = "Registrar un usuario",
            description = """
                    Crea una nueva cuenta de usuario en el sistema.

                    El usuario recibe los roles y el estado inicial establecidos
                    por las reglas de negocio del servicio.

                    Este endpoint no requiere autenticación previa.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario registrado correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = UserResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Los datos del usuario son inválidos"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Ya existe un usuario con el correo proporcionado"
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Se superó el límite de registros permitidos"
            )
    })
    @SecurityRequirements
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto register(
            @Valid
            @RequestBody
            RegisterRequestDto request
    ) {
        return service.register(request);
    }

    @Operation(
            summary = "Consultar el usuario autenticado",
            description = """
                    Devuelve los datos del usuario asociado al token JWT
                    enviado en la solicitud.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario autenticado obtenido correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = UserResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "El token no fue enviado, expiró o no es válido"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El usuario autenticado no fue encontrado"
            )
    })
    @GetMapping("/me")
    public UserResponseDto me(Principal principal) {
        return service.findAuthenticatedUser(
                principal.getName()
        );
    }

    @Operation(
            summary = "Renovar los tokens",
            description = """
                    Genera un nuevo token de acceso y rota el token de
                    actualización.

                    El token de actualización anterior queda revocado después
                    de utilizarse correctamente.

                    Este endpoint no requiere un token JWT de acceso.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tokens renovados correctamente",
                    content = @Content(
                            schema = @Schema(
                                    implementation = AuthResponseDto.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "La solicitud es inválida"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "El token de actualización es inválido, expiró o fue revocado"
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Se superó el límite de renovaciones permitidas"
            )
    })
    @SecurityRequirements
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(
            @Valid
            @RequestBody
            RefreshRequestDto request,

            HttpServletRequest httpRequest
    ) {
        String clientIp = extractClientIp(httpRequest);

        return ResponseEntity.ok(
                service.refresh(request, clientIp)
        );
    }

    @Operation(
            summary = "Cerrar sesión",
            description = """
                    Revoca el token de actualización enviado, evitando que
                    pueda utilizarse nuevamente para generar tokens.

                    Este endpoint no requiere un token JWT de acceso.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Sesión cerrada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "La solicitud es inválida"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "El token de actualización es inválido"
            )
    })
    @SecurityRequirements
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @Valid
            @RequestBody
            RefreshRequestDto request
    ) {
        service.logout(request);
    }

    private String extractClientIp(
            HttpServletRequest request
    ) {
        String forwardedFor =
                request.getHeader("X-Forwarded-For");

        if (forwardedFor != null
                && !forwardedFor.isBlank()) {

            return forwardedFor
                    .split(",")[0]
                    .trim();
        }

        return request.getRemoteAddr();
    }
}