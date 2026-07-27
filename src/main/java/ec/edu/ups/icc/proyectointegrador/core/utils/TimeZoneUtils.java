package ec.edu.ups.icc.proyectointegrador.core.utils;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class TimeZoneUtils {

    public static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Guayaquil");

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private TimeZoneUtils() {
    }

    public static ZonedDateTime toBusinessZone(OffsetDateTime instant) {
        return instant == null ? null : instant.atZoneSameInstant(BUSINESS_ZONE);
    }

    public static String format(OffsetDateTime instant) {
        ZonedDateTime zoned = toBusinessZone(instant);
        return zoned == null ? "" : zoned.format(DISPLAY_FORMAT);
    }
}