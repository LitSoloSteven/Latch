package ni.edu.uam.latch.modelo;

import ni.edu.uam.latch.dao.ConexionBD;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias del Modelo Inquilino y Reglas de Negocio")
class InquilinoTest {

    @Test
    @DisplayName("Crear inquilino valido con todos los campos")
    void testCrearInquilinoCompleto() {
        UUID id = UUID.randomUUID();
        UUID creadoPor = UUID.randomUUID();
        OffsetDateTime ahora = OffsetDateTime.now();

        Inquilino inq = new Inquilino(
                id,
                "Carlos Mendoza",
                "001-150392-0004X",
                "+505 8899-7711",
                "carlos@email.com",
                "Elena Ruiz - 8811-2233",
                true,
                ahora,
                creadoPor,
                0L
        );

        assertEquals(id, inq.getId());
        assertEquals("Carlos Mendoza", inq.getNombre());
        assertEquals("001-150392-0004X", inq.getIdentificacion());
        assertEquals("+505 8899-7711", inq.getTelefono());
        assertEquals("carlos@email.com", inq.getCorreo());
        assertEquals("Elena Ruiz - 8811-2233", inq.getContactoEmergencia());
        assertTrue(inq.isActivo());
        assertEquals(ahora, inq.getCreadoEn());
        assertEquals(creadoPor, inq.getCreadoPor());
        assertEquals(0L, inq.getVersion());
    }

    @Test
    @DisplayName("Permitir campos opcionales en null (incluyendo telefono y correo)")
    void testInquilinoCamposOpcionalesNull() {
        Inquilino inq = new Inquilino(
                "Marvin Castillo",
                null,
                null,
                null,
                null,
                null
        );

        assertNotNull(inq.getId());
        assertEquals("Marvin Castillo", inq.getNombre());
        assertNull(inq.getIdentificacion());
        assertNull(inq.getTelefono());
        assertNull(inq.getCorreo());
        assertNull(inq.getContactoEmergencia());
        assertNull(inq.getCreadoPor());
        assertTrue(inq.isActivo());
        assertEquals(0L, inq.getVersion());
    }

    @Test
    @DisplayName("Rechazar nombre nulo, vacio o en blanco")
    void testRechazarNombreInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new Inquilino(null, null, null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new Inquilino("", null, null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new Inquilino("   ", null, null, null, null, null));
    }

    @Test
    @DisplayName("Rechazar nombre que exceda 160 caracteres")
    void testRechazarNombreExcedeLongitud() {
        String nombreLargo = "A".repeat(161);
        assertThrows(IllegalArgumentException.class, () -> new Inquilino(nombreLargo, null, null, null, null, null));
    }

    @Test
    @DisplayName("Rechazar identificacion que exceda 40 caracteres")
    void testRechazarIdentificacionExcedeLongitud() {
        Inquilino inq = new Inquilino();
        assertThrows(IllegalArgumentException.class, () -> inq.setIdentificacion("X".repeat(41)));
    }

    @Test
    @DisplayName("Rechazar telefono que exceda 30 caracteres")
    void testRechazarTelefonoExcedeLongitud() {
        Inquilino inq = new Inquilino();
        assertThrows(IllegalArgumentException.class, () -> inq.setTelefono("1".repeat(31)));
    }

    @Test
    @DisplayName("Validar formato de correo electronico cuando se proporciona")
    void testValidacionCorreo() {
        Inquilino inq = new Inquilino();

        // Correos validos
        assertDoesNotThrow(() -> inq.setCorreo("inquilino.test@dominio.com"));
        assertEquals("inquilino.test@dominio.com", inq.getCorreo());

        assertDoesNotThrow(() -> inq.setCorreo("usuario+alias@empresa.com.ni"));

        // Correo nulo o vacio permitido (campo opcional)
        assertDoesNotThrow(() -> inq.setCorreo(null));
        assertNull(inq.getCorreo());
        assertDoesNotThrow(() -> inq.setCorreo("  "));
        assertNull(inq.getCorreo());

        // Correos invalidos
        assertThrows(IllegalArgumentException.class, () -> inq.setCorreo("correo-sin-arroba"));
        assertThrows(IllegalArgumentException.class, () -> inq.setCorreo("usuario@"));
        assertThrows(IllegalArgumentException.class, () -> inq.setCorreo("@dominio.com"));
        assertThrows(IllegalArgumentException.class, () -> inq.setCorreo("usuario@dominio"));
    }

    @Test
    @DisplayName("Rechazar contacto de emergencia que exceda 200 caracteres")
    void testRechazarContactoEmergenciaExcedeLongitud() {
        Inquilino inq = new Inquilino();
        assertThrows(IllegalArgumentException.class, () -> inq.setContactoEmergencia("C".repeat(201)));
    }

    @Test
    @DisplayName("Rechazar version negativa conforme a ck_inquilino_version")
    void testRechazarVersionNegativa() {
        Inquilino inq = new Inquilino();
        assertThrows(IllegalArgumentException.class, () -> inq.setVersion(-1L));
        assertDoesNotThrow(() -> inq.setVersion(0L));
        assertDoesNotThrow(() -> inq.setVersion(5L));
    }

    @Test
    @DisplayName("Sanitizacion segura de credenciales en URLs de conexion")
    void testSanitizarUrlConexion() {
        String urlPlana = "jdbc:postgresql://admin:superSecret123@localhost:5432/latch";
        String sanitizada = ConexionBD.sanitizarUrl(urlPlana);

        assertFalse(sanitizada.contains("superSecret123"), "La contrasena no debe aparecer en la URL sanitizada");
        assertTrue(sanitizada.contains("*****"), "La contrasena debe estar enmascarada");

        String urlParam = "jdbc:postgresql://localhost:5432/latch?user=postgres&password=miClaveOculta";
        String sanitizadaParam = ConexionBD.sanitizarUrl(urlParam);
        assertFalse(sanitizadaParam.contains("miClaveOculta"));
        assertTrue(sanitizadaParam.contains("password=*****"));
    }
}
