package ec.edu.ups.icc.proyectointegrador.core.exceptions;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.NoSuchElementException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import ec.edu.ups.icc.proyectointegrador.core.dtos.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage())
        );

        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Los datos enviados no son válidos.", request, errors);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {

        return build(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                "Credenciales inválidas.", request, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDto> handleAuthentication(
            AuthenticationException ex, HttpServletRequest request) {

        return build(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED",
                "Debe autenticarse para acceder a este recurso.", request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        return build(HttpStatus.FORBIDDEN, "ACCESS_DENIED",
                "No tiene permisos para realizar esta acción.", request, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDataIntegrity(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        return build(HttpStatus.CONFLICT, "DATA_CONFLICT",
                "El recurso ya existe o viola una restricción de datos.", request, null);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalState(
            IllegalStateException ex, HttpServletRequest request) {

        return build(HttpStatus.CONFLICT, "BUSINESS_RULE_VIOLATION",
                ex.getMessage(), request, null);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFound(
            NoSuchElementException ex, HttpServletRequest request) {

        return build(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND",
                "El recurso solicitado no existe.", request, null);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponseDto> handleResourceNotFound(
                ResourceNotFoundException ex,
                HttpServletRequest request
        ) {
        return build(
            HttpStatus.NOT_FOUND,
            "RESOURCE_NOT_FOUND",
            ex.getMessage(),
            request,
            null
        );
        }

        @ExceptionHandler(ConflictException.class)
        public ResponseEntity<ErrorResponseDto> handleConflict(
                ConflictException ex,
               HttpServletRequest request
        ) {
            return build(
                    HttpStatus.CONFLICT,
                    "CONFLICT",
                    ex.getMessage(),
                    request,
                    null
        );
        }

        @ExceptionHandler(BusinessRuleException.class)
        public ResponseEntity<ErrorResponseDto> handleBusinessRule(
                BusinessRuleException ex,
                HttpServletRequest request
        ) {
        return build(
            HttpStatus.CONFLICT,
        "BUSINESS_RULE_VIOLATION",
                ex.getMessage(),
            request,
            null
                );
        }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleUnexpected(
            Exception ex, HttpServletRequest request) {

        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Ocurrió un error inesperado.", request, null);
    }

    private ResponseEntity<ErrorResponseDto> build(
            HttpStatus status, String code, String message,
            HttpServletRequest request, Map<String, String> errors) {

        ErrorResponseDto body = new ErrorResponseDto(
                status.value(), code, message, request.getRequestURI(), errors
        );

        return ResponseEntity.status(status).body(body);
    }


        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponseDto> handleIllegalArgument(
        IllegalArgumentException ex,
        HttpServletRequest request
        ) {
        return build(
            HttpStatus.BAD_REQUEST,
            "INVALID_ARGUMENT",
            ex.getMessage(),
            request,
            null
        );
        }

        @ExceptionHandler(InternalServerException.class)
        public ResponseEntity<ErrorResponseDto> handleInternalServer(
        InternalServerException ex,
        HttpServletRequest request
        ) {
        return build(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_ERROR",
            "Ocurrió un error interno al procesar la solicitud.",
            request,
            null
        );
        }
}