package ec.edu.ups.icc.proyectointegrador.core.exceptions;

import org.springframework.security.core.AuthenticationException;

public class InvalidRefreshTokenException extends AuthenticationException {

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}