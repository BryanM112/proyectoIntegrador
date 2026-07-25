package ec.edu.ups.icc.proyectointegrador.security.dtos;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequestDto(

        @NotBlank(message = "El refresh token es obligatorio")
        String refreshToken

) {
}