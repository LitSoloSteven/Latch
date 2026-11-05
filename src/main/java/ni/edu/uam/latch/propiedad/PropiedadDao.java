package ni.edu.uam.latch.propiedad;

import ni.edu.uam.latch.db.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class PropiedadDao {
    private static final String CAMPOS = "id, nombre, direccion, activa, creado_en, creado_por, version";
    private final Database database;

    public PropiedadDao(Database database) {
        this.database = database;
    }

    public UUID crear(String nombre, String direccion, UUID creadoPor) throws SQLException {
        validarDatos(nombre, direccion);
        String sql = "INSERT INTO latch.propiedad (nombre, direccion, creado_por) VALUES (?, ?, ?) RETURNING id";
        try (var connection = database.open();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombre);
            statement.setString(2, direccion);
            statement.setObject(3, creadoPor);
            try (var result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("El alta no devolvió un identificador.");
                }
                return result.getObject("id", UUID.class);
            }
        }
    }

    public Optional<Propiedad> buscar(UUID id) throws SQLException {
        String sql = "SELECT " + CAMPOS + " FROM latch.propiedad WHERE id = ?";
        try (var connection = database.open();
             var statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            try (var result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapear(result)) : Optional.empty();
            }
        }
    }

    public List<Propiedad> listar() throws SQLException {
        String sql = "SELECT " + CAMPOS + " FROM latch.propiedad ORDER BY nombre, id";
        try (var connection = database.open();
             var statement = connection.prepareStatement(sql);
             var result = statement.executeQuery()) {
            List<Propiedad> propiedades = new ArrayList<>();
            while (result.next()) {
                propiedades.add(mapear(result));
            }
            return propiedades;
        }
    }

    public boolean actualizar(UUID id, String nombre, String direccion, long version) throws SQLException {
        validarDatos(nombre, direccion);
        String sql = """
                UPDATE latch.propiedad SET nombre = ?, direccion = ?, version = version + 1
                WHERE id = ? AND version = ? AND activa
                """;
        try (var connection = database.open();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombre);
            statement.setString(2, direccion);
            statement.setObject(3, id);
            statement.setLong(4, version);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean desactivar(UUID id, long version) throws SQLException {
        String sql = "UPDATE latch.propiedad SET activa = FALSE, version = version + 1 "
                + "WHERE id = ? AND version = ? AND activa";
        try (var connection = database.open();
             var statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            statement.setLong(2, version);
            return statement.executeUpdate() == 1;
        }
    }

    private static void validarDatos(String nombre, String direccion) {
        if (nombre == null || nombre.isBlank() || nombre.codePointCount(0, nombre.length()) > 120) {
            throw new IllegalArgumentException("El nombre debe tener entre 1 y 120 caracteres.");
        }
        if (direccion == null || direccion.isBlank() || direccion.codePointCount(0, direccion.length()) > 300) {
            throw new IllegalArgumentException("La dirección debe tener entre 1 y 300 caracteres.");
        }
    }

    private static Propiedad mapear(ResultSet result) throws SQLException {
        return new Propiedad(
                result.getObject("id", UUID.class),
                result.getString("nombre"),
                result.getString("direccion"),
                result.getBoolean("activa"),
                result.getObject("creado_en", OffsetDateTime.class),
                result.getObject("creado_por", UUID.class),
                result.getLong("version")
        );
    }
}
