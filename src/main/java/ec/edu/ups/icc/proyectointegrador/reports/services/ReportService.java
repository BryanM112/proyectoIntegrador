package ec.edu.ups.icc.proyectointegrador.reports.services;

import java.time.LocalDate;

import org.springframework.security.core.Authentication;

public interface ReportService {

    byte[] generateRegistrationsPdf(Long eventId, LocalDate from, LocalDate to, Authentication authentication);

    byte[] generateRegistrationsExcel(Long eventId, LocalDate from, LocalDate to, Authentication authentication);

    byte[] generateCertificatePdf(Long registrationId, Authentication authentication);
}