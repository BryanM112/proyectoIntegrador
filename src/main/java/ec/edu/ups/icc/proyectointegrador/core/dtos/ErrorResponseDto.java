package ec.edu.ups.icc.proyectointegrador.core.dtos;

import java.time.OffsetDateTime;
import java.util.Map;

public class ErrorResponseDto {
    private final OffsetDateTime timestamp;
    private final int status;
    private final String code;
    private final String message;
    private final String path;
    private final Map<String, String> errors;

    public ErrorResponseDto(int status, String code, String message, String path, Map<String, String> errors) {
        this.timestamp = OffsetDateTime.now();
        this.status = status;
        this.code = code;
        this.message = message;
        this.path = path;
        this.errors = errors;
    }

    public OffsetDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public Map<String, String> getErrors() { return errors; }
}