package ec.edu.ups.icc.proyectointegrador.core.exceptions;

public class InternalServerException extends RuntimeException {

    public InternalServerException(String message, Throwable cause) {
        super(message, cause);
    }
}