package ec.edu.ups.icc.proyectointegrador.reports.controllers;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.ups.icc.proyectointegrador.reports.services.ReportService;

@RestController
public class ReportController {

    private static final MediaType XLSX_MEDIA_TYPE =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping(value = "/reports/events/{eventId}/registrations.pdf")
    public ResponseEntity<byte[]> registrationsPdf(
            @PathVariable Long eventId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication
    ) {
        byte[] content = reportService.generateRegistrationsPdf(eventId, from, to, authentication);
        String filename = "registrations-event-" + eventId + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(content);
    }

    @GetMapping(value = "/reports/events/{eventId}/registrations.xlsx")
    public ResponseEntity<byte[]> registrationsExcel(
            @PathVariable Long eventId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication
    ) {
        byte[] content = reportService.generateRegistrationsExcel(eventId, from, to, authentication);
        String filename = "registrations-event-" + eventId + ".xlsx";

        return ResponseEntity.ok()
                .contentType(XLSX_MEDIA_TYPE)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(content);
    }

    @GetMapping(value = "/registrations/{id}/certificate.pdf")
    public ResponseEntity<byte[]> certificatePdf(
            @PathVariable Long id,
            Authentication authentication
    ) {
        byte[] content = reportService.generateCertificatePdf(id, authentication);
        String filename = "certificate-registration-" + id + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(content);
    }
}