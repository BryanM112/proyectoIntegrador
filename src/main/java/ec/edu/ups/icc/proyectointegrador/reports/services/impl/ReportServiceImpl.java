package ec.edu.ups.icc.proyectointegrador.reports.services.impl;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

import ec.edu.ups.icc.proyectointegrador.core.exceptions.BusinessRuleException;
import ec.edu.ups.icc.proyectointegrador.core.exceptions.InternalServerException;
import ec.edu.ups.icc.proyectointegrador.core.exceptions.ResourceNotFoundException;

import java.time.ZonedDateTime;
import java.util.Date;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import ec.edu.ups.icc.proyectointegrador.core.utils.TimeZoneUtils;
import ec.edu.ups.icc.proyectointegrador.events.entities.EventEntity;
import ec.edu.ups.icc.proyectointegrador.events.repositories.EventRepository;
import ec.edu.ups.icc.proyectointegrador.registrations.entities.RegistrationEntity;
import ec.edu.ups.icc.proyectointegrador.registrations.enums.RegistrationStatus;
import ec.edu.ups.icc.proyectointegrador.registrations.repositories.RegistrationRepository;
import ec.edu.ups.icc.proyectointegrador.reports.services.ReportService;
import ec.edu.ups.icc.proyectointegrador.users.entities.UserEntity;
import ec.edu.ups.icc.proyectointegrador.users.repositories.UserRepository;

@Service
public class ReportServiceImpl implements ReportService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;

    public ReportServiceImpl(
            EventRepository eventRepository,
            RegistrationRepository registrationRepository,
            UserRepository userRepository
    ) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateRegistrationsPdf(Long eventId, LocalDate from, LocalDate to, Authentication authentication) {
        validateDateRange(from, to);
        EventEntity event = findEventOrThrow(eventId);
        requireOwnerOrAdmin(event, authentication);

        List<RegistrationEntity> registrations = filterByDateRange(
                registrationRepository.findAllByEventIdOrderByRegisteredAtDesc(eventId), from, to);

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, output);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            document.add(new Paragraph("Listado de inscritos - " + event.getTitle(), titleFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            addHeaderCell(table, "Participante");
            addHeaderCell(table, "Email");
            addHeaderCell(table, "Estado");
            addHeaderCell(table, "Fecha de inscripcion");

            for (RegistrationEntity registration : registrations) {
                UserEntity participant = registration.getParticipant();
                table.addCell(participant.getFirstName() + " " + participant.getLastName());
                table.addCell(participant.getEmail());
                table.addCell(registration.getStatus().name());
                table.addCell(TimeZoneUtils.format(registration.getRegisteredAt()));
            }

            document.add(table);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Total de inscritos: " + registrations.size()));

            document.close();
            return output.toByteArray();
        } catch (Exception ex) {
            throw new InternalServerException("No se pudo generar el reporte PDF", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateRegistrationsExcel(Long eventId, LocalDate from, LocalDate to, Authentication authentication) {
        validateDateRange(from, to);
        EventEntity event = findEventOrThrow(eventId);
        requireOwnerOrAdmin(event, authentication);

        List<RegistrationEntity> registrations = filterByDateRange(
                registrationRepository.findAllByEventIdOrderByRegisteredAtDesc(eventId), from, to);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Inscritos");

            CreationHelper creationHelper =
            workbook.getCreationHelper();

            CellStyle dateStyle =
            workbook.createCellStyle();

            dateStyle.setDataFormat(
                creationHelper
                    .createDataFormat()
                    .getFormat("dd/mm/yyyy hh:mm")
            );

            Row header = sheet.createRow(0);
            String[] columns = {"Participante", "Email", "Estado", "Fecha de inscripcion", "Codigo"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
            }

            int rowIndex = 1;
            for (RegistrationEntity registration : registrations) {
                Row row = sheet.createRow(rowIndex++);
                UserEntity participant = registration.getParticipant();

                row.createCell(0).setCellValue(participant.getFirstName() + " " + participant.getLastName());
                row.createCell(1).setCellValue(participant.getEmail());
                row.createCell(2).setCellValue(registration.getStatus().name());
                
                ZonedDateTime businessDate =
                    TimeZoneUtils.toBusinessZone(
                        registration.getRegisteredAt()
                    );

                Cell dateCell = row.createCell(3);

                if (businessDate != null) {
                    dateCell.setCellValue(
                        Date.from(
                            businessDate.toInstant()
                            )
                        );

                    dateCell.setCellStyle(dateStyle);
                }


                row.createCell(4).setCellValue(registration.getRegistrationCode().toString());
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            workbook.write(output);
            return output.toByteArray();
        } catch (Exception ex) {
            throw new InternalServerException("No se pudo generar el reporte Excel", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateCertificatePdf(Long registrationId, Authentication authentication) {
        RegistrationEntity registration = registrationRepository.findWithRelationsById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción no encontrada"));

        UserEntity currentUser = resolveCurrentUser(authentication);

        if (!registration.getParticipant().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Solo el participante propietario puede descargar este comprobante");
        }

        if (registration.getStatus() != RegistrationStatus.CONFIRMED) {
            throw new BusinessRuleException("La inscripción no está confirmada");
        }

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, output);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 12, Font.NORMAL);

            Paragraph title = new Paragraph("Comprobante de Inscripcion", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            UserEntity participant = registration.getParticipant();
            EventEntity event = registration.getEvent();

            document.add(new Paragraph("Participante: " + participant.getFirstName() + " " + participant.getLastName(), normalFont));
            document.add(new Paragraph("Email: " + participant.getEmail(), normalFont));
            document.add(new Paragraph("Evento: " + event.getTitle(), normalFont));
            document.add(new Paragraph("Fecha del evento: " + TimeZoneUtils.format(event.getStartAt()), normalFont));
            document.add(new Paragraph("Codigo de inscripcion: " + registration.getRegistrationCode(), normalFont));
            document.add(new Paragraph("Estado: CONFIRMADA", normalFont));
            document.add(new Paragraph("Emitido el: " + TimeZoneUtils.format(registration.getConfirmedAt()), normalFont));

            document.close();
            return output.toByteArray();
        } catch (Exception ex) {
            throw new InternalServerException("No se pudo generar el comprobante", ex);
        }
    }

    private EventEntity findEventOrThrow(Long eventId) {
        return eventRepository.findByIdAndDeletedFalse(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado"));
    }

private UserEntity resolveCurrentUser(
        Authentication authentication
) {
    if (authentication == null
            || !authentication.isAuthenticated()) {

        throw new ResourceNotFoundException(
                "Usuario autenticado no encontrado"
        );
    }

    return userRepository
            .findWithRolesByEmail(
                    authentication.getName()
            )
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Usuario autenticado no encontrado"
                    )
            );
}

    private void requireOwnerOrAdmin(EventEntity event, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return;
        }

        UserEntity currentUser = resolveCurrentUser(authentication);

        if (!event.getOrganizer().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Solo el organizador propietario o un administrador pueden generar este reporte");
        }
    }

    private List<RegistrationEntity> filterByDateRange(List<RegistrationEntity> registrations, LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return registrations;
        }

        return registrations.stream()
                .filter(registration -> {
                    LocalDate registeredDate = TimeZoneUtils.toBusinessZone(registration.getRegisteredAt()).toLocalDate();
                    boolean afterFrom = from == null || !registeredDate.isBefore(from);
                    boolean beforeTo = to == null || !registeredDate.isAfter(to);
                    return afterFrom && beforeTo;
                })
                .toList();
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, new Font(Font.HELVETICA, 11, Font.BOLD)));
        table.addCell(cell);
    }


    private void validateDateRange(
        LocalDate from,
        LocalDate to
) {
    if (from != null
            && to != null
            && from.isAfter(to)) {

        throw new BusinessRuleException(
                "La fecha inicial no puede ser posterior a la fecha final"
        );
    }
}


}