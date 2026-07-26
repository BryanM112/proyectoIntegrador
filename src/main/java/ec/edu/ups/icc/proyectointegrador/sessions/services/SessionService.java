package ec.edu.ups.icc.proyectointegrador.sessions.services;

import java.util.List;

import ec.edu.ups.icc.proyectointegrador.sessions.dtos.CreateSessionDto;
import ec.edu.ups.icc.proyectointegrador.sessions.dtos.SessionResponseDto;
import ec.edu.ups.icc.proyectointegrador.sessions.dtos.UpdateSessionDto;

public interface SessionService {

    List<SessionResponseDto> findByEvent(Long eventId, String authenticatedEmail);

    SessionResponseDto findById(Long id, String authenticatedEmail);

    SessionResponseDto create(CreateSessionDto dto, String authenticatedEmail);

    SessionResponseDto update(Long id, UpdateSessionDto dto, String authenticatedEmail);

    void delete(Long id, String authenticatedEmail);
}