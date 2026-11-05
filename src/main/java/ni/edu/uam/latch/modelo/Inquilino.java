package ni.edu.uam.latch.modelo;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Modelo de dominio para la entidad Inquilino.
 * Mapea la tabla latch.inquilino de acuerdo con el esquema y diccionario de datos
 * (docs/Latch_Backend_Schema_APA7_v3.docx, Apendice A y Tabla D5).
 */
public class Inquilino {

    private static final int MAX_NOMBRE = 160;
    private static final int MAX_IDENTIFICACION = 40;
    private static final int MAX_TELEFONO = 30;
    private static final int MAX_CORREO = 254;
    private static final int MAX_CONTACTO_EMERGENCIA = 200;

    private static final Pattern PATRON_CORREO = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private UUID id;
    private String nombre;
    private String identificacion;
    private String telefono;
    private String correo;
    private String contactoEmergencia;
    private boolean activo;
    private OffsetDateTime creadoEn;
    private UUID creadoPor;
    private long version;

    /**
     * Constructor por defecto para deserializacion o mapeo JDBC.
     */
    public Inquilino() {
        this.activo = true;
        this.version = 0L;
    }

    /**
     * Constructor de conveniencia para creacion de nuevas instancias de inquilino.
     * Genera un UUID y fecha actual por defecto.
     */
    public Inquilino(String nombre, String identificacion, String telefono,
                     String correo, String contactoEmergencia, UUID creadoPor) {
        this.id = UUID.randomUUID();
        setNombre(nombre);
        setIdentificacion(identificacion);
        setTelefono(telefono);
        setCorreo(correo);
        setContactoEmergencia(contactoEmergencia);
        this.activo = true;
        this.creadoEn = OffsetDateTime.now();
        this.creadoPor = creadoPor;
        this.version = 0L;
    }

    /**
     * Constructor completo con todos los atributos persistentes.
     */
    public Inquilino(UUID id, String nombre, String identificacion, String telefono,
                     String correo, String contactoEmergencia, boolean activo,
                     OffsetDateTime creadoEn, UUID creadoPor, long version) {
        this.id = id;
        setNombre(nombre);
        setIdentificacion(identificacion);
        setTelefono(telefono);
        setCorreo(correo);
        setContactoEmergencia(contactoEmergencia);
        this.activo = activo;
        this.creadoEn = creadoEn;
        this.creadoPor = creadoPor;
        setVersion(version);
    }

    // --- Getters y Setters con validaciones de negocio ---

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del inquilino es obligatorio.");
        }
        String limpio = nombre.trim();
        if (limpio.length() > MAX_NOMBRE) {
            throw new IllegalArgumentException("El nombre no puede exceder los " + MAX_NOMBRE + " caracteres.");
        }
        this.nombre = limpio;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        if (identificacion != null) {
            String limpio = identificacion.trim();
            if (limpio.isEmpty()) {
                this.identificacion = null;
                return;
            }
            if (limpio.length() > MAX_IDENTIFICACION) {
                throw new IllegalArgumentException("La identificacion no puede exceder los " + MAX_IDENTIFICACION + " caracteres.");
            }
            this.identificacion = limpio;
        } else {
            this.identificacion = null;
        }
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        if (telefono != null) {
            String limpio = telefono.trim();
            if (limpio.isEmpty()) {
                this.telefono = null;
                return;
            }
            if (limpio.length() > MAX_TELEFONO) {
                throw new IllegalArgumentException("El telefono no puede exceder los " + MAX_TELEFONO + " caracteres.");
            }
            this.telefono = limpio;
        } else {
            this.telefono = null;
        }
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        if (correo != null) {
            String limpio = correo.trim();
            if (limpio.isEmpty()) {
                this.correo = null;
                return;
            }
            if (limpio.length() > MAX_CORREO) {
                throw new IllegalArgumentException("El correo electronico no puede exceder los " + MAX_CORREO + " caracteres.");
            }
            if (!PATRON_CORREO.matcher(limpio).matches()) {
                throw new IllegalArgumentException("El formato del correo electronico es invalido: " + limpio);
            }
            this.correo = limpio;
        } else {
            this.correo = null;
        }
    }

    public String getContactoEmergencia() {
        return contactoEmergencia;
    }

    public void setContactoEmergencia(String contactoEmergencia) {
        if (contactoEmergencia != null) {
            String limpio = contactoEmergencia.trim();
            if (limpio.isEmpty()) {
                this.contactoEmergencia = null;
                return;
            }
            if (limpio.length() > MAX_CONTACTO_EMERGENCIA) {
                throw new IllegalArgumentException("El contacto de emergencia no puede exceder los " + MAX_CONTACTO_EMERGENCIA + " caracteres.");
            }
            this.contactoEmergencia = limpio;
        } else {
            this.contactoEmergencia = null;
        }
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public OffsetDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(OffsetDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public UUID getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(UUID creadoPor) {
        this.creadoPor = creadoPor;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        if (version < 0) {
            throw new IllegalArgumentException("La version no puede ser negativa (ck_inquilino_version).");
        }
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Inquilino inquilino = (Inquilino) o;
        return Objects.equals(id, inquilino.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Inquilino{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", identificacion='" + identificacion + '\'' +
                ", telefono='" + telefono + '\'' +
                ", correo='" + correo + '\'' +
                ", contactoEmergencia='" + contactoEmergencia + '\'' +
                ", activo=" + activo +
                ", creadoEn=" + creadoEn +
                ", creadoPor=" + creadoPor +
                ", version=" + version +
                '}';
    }
}
