package ni.edu.uam.latch.db;

import org.postgresql.ds.PGSimpleDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class Database {
    private final PGSimpleDataSource dataSource;

    private Database(PGSimpleDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static Database fromEnvironment() {
        String url = required("DB_URL");
        if (!url.startsWith("jdbc:postgresql://") || url.contains("?")) {
            throw new IllegalArgumentException("DB_URL debe usar jdbc:postgresql://host:puerto/base.");
        }

        PGSimpleDataSource source = new PGSimpleDataSource();
        source.setURL(url);
        source.setUser(required("DB_USER"));
        String password = System.getenv("DB_PASSWORD");
        if (password == null) {
            throw new IllegalArgumentException("Falta configurar DB_PASSWORD.");
        }
        source.setPassword(password);
        source.setConnectTimeout(5);
        source.setSocketTimeout(10);
        return new Database(source);
    }

    public Connection open() throws SQLException {
        return dataSource.getConnection();
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Falta configurar " + name + ".");
        }
        return value;
    }
}
