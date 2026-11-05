package ni.edu.uam.latch.usuario;

import ni.edu.uam.latch.db.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class UsuarioDao {
    private static final String CAMPOS = """
            id, rol_id, nombre_usuario, nombre_visible, cambio_inicial, intentos_fallidos,
            bloqueado_hasta, activo, creado_en, creado_por, version
            """;

    private final Database database;

    public UsuarioDao(Database database) {
        this.database = database;
    }

    public UUID crear(UUID rolId, String nombreUsuario, String nombreVisible, String hashClave, UUID creadoPor)
            throws SQLException {
        String sql = """
                INSERT INTO latch.usuario (rol_id, nombre_usuario, nombre_visible, hash_clave, creado_por)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
                """;
        try (var connection = database.open();
             var statement = connection.prepareStatement(sql)) {
            statement.setObject(1, rolId);
            statement.setString(2, nombreUsuario);
            statement.setString(3, nombreVisible);
            statement.setString(4, hashClave);
            statement.setObject(5, creadoPor);
            try (var result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("El alta no devolvió un identificador.");
                }
                return result.getObject("id", UUID.class);
            }
        }
    }

    public Optional<Usuario> buscar(UUID id) throws SQLException {
        String sql = "SELECT " + CAMPOS + " FROM latch.usuario WHERE id = ?";
        try (var connection = database.open();
             var statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            try (var result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapear(result)) : Optional.empty();
            }
        }
    }

    public List<Usuario> listar() throws SQLException {
        String sql = "SELECT " + CAMPOS + " FROM latch.usuario ORDER BY nombre_usuario, id";
        try (var connection = database.open();
             var statement = connection.prepareStatement(sql);
             var result = statement.executeQuery()) {
            List<Usuario> usuarios = new ArrayList<>();
            while (result.next()) {
                usuarios.add(mapear(result));
            }
            return usuarios;
        }
    }

    public boolean cambiarNombreVisible(UUID id, String nombreVisible) throws SQLException {
        String sql = "UPDATE latch.usuario SET nombre_visible = ?, version = version + 1 WHERE id = ?";
        try (var connection = database.open();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombreVisible);
            statement.setObject(2, id);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean desactivar(UUID id) throws SQLException {
        String sql = "UPDATE latch.usuario SET activo = FALSE, version = version + 1 WHERE id = ? AND activo";
        try (var connection = database.open();
             var statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    private static Usuario mapear(ResultSet result) throws SQLException {
        return new Usuario(
                result.getObject("id", UUID.class),
                result.getObject("rol_id", UUID.class),
                result.getString("nombre_usuario"),
                result.getString("nombre_visible"),
                result.getBoolean("cambio_inicial"),
                result.getInt("intentos_fallidos"),
                result.getObject("bloqueado_hasta", OffsetDateTime.class),
                result.getBoolean("activo"),
                result.getObject("creado_en", OffsetDateTime.class),
                result.getObject("creado_por", UUID.class),
                result.getLong("version")
        );
    }
}
