package ni.edu.uam.latch.modelo;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(schema = "latch", name = "propiedad")
public class Propiedad {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nombre", length = 120, nullable = false)
    private String nombre;

    @Column(name = "direccion", length = 300, nullable = false)
    private String direccion;

    @Column(name = "activa", nullable = false)
    private boolean activa = true;

    @Column(name = "creado_en", nullable = false)
    private ZonedDateTime creadoEn;

    @Column(name = "creado_por")
    private UUID creadoPor;

    @Version
    @Column(name = "version")
    private Long version = 0L;

    public Propiedad() {
        this.creadoEn = ZonedDateTime.now();
    }

    public Propiedad(String nombre, String direccion, UUID creadoPor) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.creadoPor = creadoPor;
        this.creadoEn = ZonedDateTime.now();
        this.activa = true;
    }

    // Getters
    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public boolean isActiva() { return activa; }
    public ZonedDateTime getCreadoEn() { return creadoEn; }
    public UUID getCreadoPor() { return creadoPor; }
    public Long getVersion() { return version; }

    // Setters
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public void setActiva(boolean activa) { this.activa = activa; }
}
