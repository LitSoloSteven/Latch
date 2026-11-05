package ni.edu.uam.latch.dao;

import ni.edu.uam.latch.modelo.Inquilino;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Data Access Object (DAO) para la persistencia JDBC de la entidad Inquilino.
 * Cumple con el acuerdo tecnico de Latch:
 * - Consultas parametrizadas sobre el esquema latch.inquilino.
 * - Cierre de recursos mediante try-with-resources.
 * - Control de concurrencia optimista con incremento de version.
 * - Baja logica mediante activo=false (sin borrado fisico).
 */
public class InquilinoDao {

    @FunctionalInterface
    public interface ConnectionSupplier {
        Connection getConnection() throws SQLException;
    }

    private final ConnectionSupplier connectionSupplier;

    public InquilinoDao() {
        this(ConexionBD::obtenerConexion);
    }

    public InquilinoDao(ConnectionSupplier connectionSupplier) {
        this.connectionSupplier = Objects.requireNonNull(connectionSupplier, "ConnectionSupplier no puede ser nulo.");
    }

    /**
     * Inserta un nuevo registro en latch.inquilino.
     *
     * @param inquilino Instancia con los datos a persistir.
     * @return El inquilino persistido con su ID y version asignados.
     * @throws SQLException Si ocurre un error de base de datos o violacion de restriccion.
     */
    public Inquilino insertar(Inquilino inquilino) throws SQLException {
        Objects.requireNonNull(inquilino, "El inquilino a insertar no puede ser nulo.");
        if (inquilino.getId() == null) {
            inquilino.setId(UUID.randomUUID());
        }
        if (inquilino.getCreadoEn() == null) {
            inquilino.setCreadoEn(OffsetDateTime.now());
        }

        final String sql = "INSERT INTO latch.inquilino (" +
                "id, nombre, identificacion, telefono, correo, contacto_emergencia, activo, creado_en, creado_por, version" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connectionSupplier.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, inquilino.getId());
            ps.setString(2, inquilino.getNombre());
            ps.setString(3, inquilino.getIdentificacion());
            ps.setString(4, inquilino.getTelefono());
            ps.setString(5, inquilino.getCorreo());
            ps.setString(6, inquilino.getContactoEmergencia());
            ps.setBoolean(7, inquilino.isActivo());
            ps.setObject(8, inquilino.getCreadoEn());
            ps.setObject(9, inquilino.getCreadoPor());
            ps.setLong(10, inquilino.getVersion());

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new SQLException("La insercion del inquilino no afecto ninguna fila.");
            }
            return inquilino;
        }
    }

    /**
     * Busca un inquilino por su identificador unico (UUID).
     *
     * @param id UUID del inquilino.
     * @return Optional con el inquilino si existe, o vacio si no se encuentra.
     * @throws SQLException En caso de error de acceso a datos.
     */
    public Optional<Inquilino> buscarPorId(UUID id) throws SQLException {
        Objects.requireNonNull(id, "El ID de busqueda no puede ser nulo.");

        final String sql = "SELECT id, nombre, identificacion, telefono, correo, contacto_emergencia, activo, creado_en, creado_por, version " +
                "FROM latch.inquilino WHERE id = ?";

        try (Connection conn = connectionSupplier.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearFila(rs));
                }
                return Optional.empty();
            }
        }
    }

    /**
     * Lista todos los inquilinos registrados en la base de datos ordenados por fecha de creacion.
     *
     * @return Lista de inquilinos.
     * @throws SQLException En caso de error en la consulta.
     */
    public List<Inquilino> listarTodos() throws SQLException {
        final String sql = "SELECT id, nombre, identificacion, telefono, correo, contacto_emergencia, activo, creado_en, creado_por, version " +
                "FROM latch.inquilino ORDER BY creado_en DESC";

        try (Connection conn = connectionSupplier.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Inquilino> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapearFila(rs));
            }
            return lista;
        }
    }

    /**
     * Lista unicamente los inquilinos con estado activo = true.
     *
     * @return Lista de inquilinos activos.
     * @throws SQLException En caso de error en la consulta.
     */
    public List<Inquilino> listarActivos() throws SQLException {
        final String sql = "SELECT id, nombre, identificacion, telefono, correo, contacto_emergencia, activo, creado_en, creado_por, version " +
                "FROM latch.inquilino WHERE activo = TRUE ORDER BY nombre ASC";

        try (Connection conn = connectionSupplier.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Inquilino> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(mapearFila(rs));
            }
            return lista;
        }
    }

    /**
     * Actualiza los datos de contacto de un inquilino, incrementando su version optimista.
     *
     * @param id Identificador del inquilino.
     * @param telefono Nuevo telefono (puede ser null).
     * @param correo Nuevo correo electronico (puede ser null).
     * @param contactoEmergencia Nuevo contacto de emergencia (puede ser null).
     * @param versionEsperada Version concurrente actual requerida.
     * @return true si la actualizacion fue exitosa, false si hubo conflicto de version o no existe.
     * @throws SQLException En caso de error de base de datos.
     */
    public boolean actualizarContacto(UUID id, String telefono, String correo,
                                      String contactoEmergencia, long versionEsperada) throws SQLException {
        Objects.requireNonNull(id, "El ID no puede ser nulo.");

        // Validaciones previas de formato de dominio
        Inquilino validador = new Inquilino();
        validador.setTelefono(telefono);
        validador.setCorreo(correo);
        validador.setContactoEmergencia(contactoEmergencia);

        final String sql = "UPDATE latch.inquilino SET " +
                "telefono = ?, correo = ?, contacto_emergencia = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?";

        try (Connection conn = connectionSupplier.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, validador.getTelefono());
            ps.setString(2, validador.getCorreo());
            ps.setString(3, validador.getContactoEmergencia());
            ps.setObject(4, id);
            ps.setLong(5, versionEsperada);

            int filas = ps.executeUpdate();
            return filas > 0;
        }
    }

    /**
     * Actualiza de forma completa los campos editables de un inquilino incrementando su version.
     *
     * @param inquilino Inquilino con los datos a actualizar.
     * @return true si se actualizo, false si la version en BD no coincide.
     * @throws SQLException En caso de fallo de BD.
     */
    public boolean actualizar(Inquilino inquilino) throws SQLException {
        Objects.requireNonNull(inquilino, "El inquilino no puede ser nulo.");
        Objects.requireNonNull(inquilino.getId(), "El ID del inquilino no puede ser nulo.");

        final String sql = "UPDATE latch.inquilino SET " +
                "nombre = ?, identificacion = ?, telefono = ?, correo = ?, contacto_emergencia = ?, activo = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?";

        try (Connection conn = connectionSupplier.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, inquilino.getNombre());
            ps.setString(2, inquilino.getIdentificacion());
            ps.setString(3, inquilino.getTelefono());
            ps.setString(4, inquilino.getCorreo());
            ps.setString(5, inquilino.getContactoEmergencia());
            ps.setBoolean(6, inquilino.isActivo());
            ps.setObject(7, inquilino.getId());
            ps.setLong(8, inquilino.getVersion());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                inquilino.setVersion(inquilino.getVersion() + 1);
                return true;
            }
            return false;
        }
    }

    /**
     * Aplica baja logica (activo = false) incrementando la version de registro.
     * No realiza borrado fisico (DELETE) para conservar integridad referencial histórica.
     *
     * @param id Identificador del inquilino.
     * @param versionEsperada Version concurrente esperada.
     * @return true si el inquilino fue dado de baja, false si no coincide la version o no existe.
     * @throws SQLException En caso de error de base de datos.
     */
    public boolean darDeBaja(UUID id, long versionEsperada) throws SQLException {
        Objects.requireNonNull(id, "El ID no puede ser nulo.");

        final String sql = "UPDATE latch.inquilino SET activo = FALSE, version = version + 1 " +
                "WHERE id = ? AND version = ?";

        try (Connection conn = connectionSupplier.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id);
            ps.setLong(2, versionEsperada);

            int filas = ps.executeUpdate();
            return filas > 0;
        }
    }

    /**
     * Mapea un ResultSet a una instancia de Inquilino.
     */
    private Inquilino mapearFila(ResultSet rs) throws SQLException {
        UUID id = (UUID) rs.getObject("id");
        String nombre = rs.getString("nombre");
        String identificacion = rs.getString("identificacion");
        String telefono = rs.getString("telefono");
        String correo = rs.getString("correo");
        String contactoEmergencia = rs.getString("contacto_emergencia");
        boolean activo = rs.getBoolean("activo");
        OffsetDateTime creadoEn = rs.getObject("creado_en", OffsetDateTime.class);
        UUID creadoPor = (UUID) rs.getObject("creado_por");
        long version = rs.getLong("version");

        return new Inquilino(id, nombre, identificacion, telefono, correo, contactoEmergencia, activo, creadoEn, creadoPor, version);
    }
}
