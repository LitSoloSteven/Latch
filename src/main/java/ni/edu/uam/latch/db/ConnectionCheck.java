package ni.edu.uam.latch.db;

import java.sql.SQLException;

public final class ConnectionCheck {
    private ConnectionCheck() {
    }

    public static void main(String[] args) {
        try {
            Database database = Database.fromEnvironment();
            try (var connection = database.open();
                 var statement = connection.prepareStatement("SELECT 1");
                 var result = statement.executeQuery()) {
                if (!result.next() || result.getInt(1) != 1) {
                    throw new SQLException("Resultado de conexión inesperado.");
                }
            }
            System.out.println("PostgreSQL: SELECT 1 -> 1");
        } catch (IllegalArgumentException exception) {
            System.err.println("Configuración de PostgreSQL inválida. Revisa DB_URL, DB_USER y DB_PASSWORD.");
            System.exit(1);
        } catch (SQLException exception) {
            System.err.println("No se pudo comprobar la conexión a PostgreSQL.");
            System.exit(1);
        }
    }
}
