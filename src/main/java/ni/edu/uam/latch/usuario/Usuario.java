package ni.edu.uam.latch.usuario;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Usuario(
        UUID id,
        UUID rolId,
        String nombreUsuario,
        String nombreVisible,
        boolean cambioInicial,
        int intentosFallidos,
        OffsetDateTime bloqueadoHasta,
        boolean activo,
        OffsetDateTime creadoEn,
        UUID creadoPor,
        long version
) {}
