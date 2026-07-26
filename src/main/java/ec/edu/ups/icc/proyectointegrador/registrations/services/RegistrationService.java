package ec.edu.ups.icc.proyectointegrador.registrations.services;

import java.util.List;
import java.util.UUID;

import ec.edu.ups.icc.proyectointegrador.registrations.dtos.CancelRegistrationDto;
import ec.edu.ups.icc.proyectointegrador.registrations.dtos.CreateRegistrationDto;
import ec.edu.ups.icc.proyectointegrador.registrations.dtos.RegistrationResponseDto;
import ec.edu.ups.icc.proyectointegrador.registrations.dtos.UpdateRegistrationStatusDto;
import ec.edu.ups.icc.proyectointegrador.registrations.enums.RegistrationStatus;

public interface RegistrationService {

    RegistrationResponseDto create(
            CreateRegistrationDto dto,
            String authenticatedEmail
    );

    RegistrationResponseDto findById(
            Long id,
            String authenticatedEmail
    );

    RegistrationResponseDto findByCode(
            UUID registrationCode,
            String authenticatedEmail
    );

    List<RegistrationResponseDto> findMyRegistrations(
            String authenticatedEmail
    );

    List<RegistrationResponseDto> findByEvent(
            Long eventId,
            RegistrationStatus status,
            String authenticatedEmail
    );

    RegistrationResponseDto updateStatus(
            Long id,
            UpdateRegistrationStatusDto dto,
            String authenticatedEmail
    );

    RegistrationResponseDto cancel(
            Long id,
            CancelRegistrationDto dto,
            String authenticatedEmail
    );
}