package ni.edu.uam.latch.propiedad;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Propiedad(
        UUID id,
        String nombre,
        String direccion,
        boolean activa,
        OffsetDateTime creadoEn,
        UUID creadoPor,
        long version
) {}
