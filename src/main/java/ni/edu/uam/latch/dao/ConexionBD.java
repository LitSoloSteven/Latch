package ni.edu.uam.latch.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fabrica compartida de conexiones JDBC para PostgreSQL.
 * Obtiene la configuracion externamente mediante variables de entorno DB_URL, DB_USER y DB_PASSWORD.
 * Protege credenciales evitando su exposicion en mensajes de error y registros.
 */
public class ConexionBD {

    private static final String URL_PREDETERMINADA = "jdbc:postgresql://localhost:5432/latch";
    private static final String USUARIO_PREDETERMINADO = "postgres";

    @FunctionalInterface
    public interface ProveedorConexion {
        Connection obtener() throws SQLException;
    }

    private static ProveedorConexion proveedorPersonalizado = null;

    /**
     * Permite fijar un proveedor personalizado (util para pruebas o delegacion a otra fabrica).
     */
    public static void setProveedorPersonalizado(ProveedorConexion proveedor) {
        proveedorPersonalizado = proveedor;
    }

    /**
     * Obtiene una conexion JDBC a PostgreSQL utilizando try-with-resources en el llamador.
     *
     * @return Connection activa
     * @throws SQLException si ocurre un error de conexion, sin exponer credenciales
     */
    public static Connection obtenerConexion() throws SQLException {
        if (proveedorPersonalizado != null) {
            return proveedorPersonalizado.obtener();
        }

        String url = System.getenv("DB_URL");
        String usuario = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        if (url == null || url.trim().isEmpty()) {
            url = URL_PREDETERMINADA;
        }
        if (usuario == null || usuario.trim().isEmpty()) {
            usuario = USUARIO_PREDETERMINADO;
        }
        if (password == null) {
            password = "";
        }

        try {
            return DriverManager.getConnection(url, usuario, password);
        } catch (SQLException e) {
            // Notificar error de conexion sin revelar la contrasena
            String urlSegura = sanitizarUrl(url);
            throw new SQLException(
                    "Error al conectar con la base de datos (" + urlSegura + ", usuario: " + usuario + "): " + e.getMessage(),
                    e.getSQLState(),
                    e.getErrorCode()
            );
        }
    }

    /**
     * Oculta contrasenas incrustadas en URLs para reportes y logs seguros.
     */
    public static String sanitizarUrl(String url) {
        if (url == null) return "null";
        return url.replaceAll("(?i)(password=)[^&;]*", "$1*****")
                  .replaceAll("(://[^:]+:)[^@]+@", "$1*****@");
    }
}
