package ec.edu.ups.icc.proyectointegrador.security.cotrollers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.ups.icc.proyectointegrador.security.dtos.AuthResponseDto;
import ec.edu.ups.icc.proyectointegrador.security.dtos.LoginRequestDto;
import ec.edu.ups.icc.proyectointegrador.security.dtos.RegisterRequestDto;
import ec.edu.ups.icc.proyectointegrador.security.services.AuthService;
import ec.edu.ups.icc.proyectointegrador.users.dtos.UserResponseDto;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import java.security.Principal;
import jakarta.servlet.http.HttpServletRequest;
import ec.edu.ups.icc.proyectointegrador.security.dtos.RefreshRequestDto;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @Valid @RequestBody LoginRequestDto request, HttpServletRequest httpRequest
    ) {
        String clientIp = extractClientIp(httpRequest);


            return ResponseEntity.ok(service.login(request, clientIp));
    }

    @SecurityRequirements
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto register(
            @Valid @RequestBody RegisterRequestDto request
    ) {
        return service.register(request);
    }



    @GetMapping("/me")
    public UserResponseDto me(Principal principal) {
        return service.findAuthenticatedUser(principal.getName());
    }


    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
           return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }


    @SecurityRequirements
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(@Valid @RequestBody RefreshRequestDto request, HttpServletRequest httpRequest) {
        String clientIp = extractClientIp(httpRequest);

        return ResponseEntity.ok(
                service.refresh(request, clientIp)
        );
    }


    @SecurityRequirements
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshRequestDto request) {
        service.logout(request);
    }
}