package ni.edu.uam.latch.usuario;

import ni.edu.uam.latch.db.Database;

import java.sql.SQLException;
import java.util.Locale;
import java.util.UUID;

public final class UsuarioDemo {
    private static final UUID ROL_ASISTENTE = UUID.fromString("d4010000-0000-4000-8000-000000000002");
    private static final UUID USUARIO_SEMILLA = UUID.fromString("d4030000-0000-4000-8000-000000000001");
    private static final String HASH_SINTETICO =
            "$argon2id$v=19$m=19456,t=2,p=1$ZvWxiyWGMN9Y+ihSXuZ6sw$6/X5CGgqKyycmckC19+BPdCglEdBw8I/xNhLTwLaMQU";
    private static final String NOMBRE_EDITADO = "Nombre actualizado de demostración";

    private UsuarioDemo() {
    }

    public static void main(String[] args) {
        try {
            Database database = Database.fromEnvironment();
            UsuarioDao dao = new UsuarioDao(database);
            if (args.length == 1 && "demostrar".equals(args[0])) {
                demostrar(database, dao);
            } else if (args.length == 2 && "verificar".equals(args[0])) {
                Usuario usuario = buscar(dao, UUID.fromString(args[1]));
                comprobarBaja(usuario);
                System.out.println("Persistencia tras reinicio confirmada: " + usuario.id());
            } else {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException exception) {
            System.err.println("Configura DB_URL, DB_USER y DB_PASSWORD. Usa demostrar o verificar <UUID>.");
            System.exit(1);
        } catch (SQLException exception) {
            System.err.println("No se pudo completar la demostración en PostgreSQL.");
            System.exit(1);
        } catch (IllegalStateException exception) {
            System.err.println(exception.getMessage());
            System.exit(1);
        }
    }

    private static void demostrar(Database database, UsuarioDao dao) throws SQLException {
        String alias = "demo_" + UUID.randomUUID().toString().replace("-", "");
        UUID id = dao.crear(ROL_ASISTENTE, alias, "Usuario sintético", HASH_SINTETICO, USUARIO_SEMILLA);
        Usuario creado = buscar(dao, id);
        exigir(creado.activo() && creado.version() == 0 && alias.equals(creado.nombreUsuario())
                && USUARIO_SEMILLA.equals(creado.creadoPor()), "Alta incorrecta.");
        exigir(dao.listar().contains(creado), "El usuario nuevo no aparece en el listado.");
        System.out.println("Alta, consulta por ID y listado confirmados.");

        exigir(dao.cambiarNombreVisible(id, NOMBRE_EDITADO), "No se actualizó el usuario.");
        Usuario editado = buscar(dao, id);
        exigir(NOMBRE_EDITADO.equals(editado.nombreVisible()) && editado.version() == 1 && editado.activo(),
                "La edición no quedó persistida.");
        System.out.println("Edición confirmada. Versión: 1.");

        exigirRechazo("23505", () -> dao.crear(ROL_ASISTENTE, alias.toUpperCase(Locale.ROOT),
                "Duplicado", HASH_SINTETICO, USUARIO_SEMILLA));
        exigirRechazo("23514", () -> {
            String sql = "UPDATE latch.usuario SET intentos_fallidos = -1, nombre_visible = ? WHERE id = ?";
            try (var connection = database.open();
                 var statement = connection.prepareStatement(sql)) {
                statement.setString(1, "No debe persistir");
                statement.setObject(2, id);
                statement.executeUpdate();
            }
        });
        exigir(editado.equals(buscar(dao, id)), "La operación inválida alteró el usuario.");
        long coincidencias = dao.listar().stream().filter(u -> u.nombreUsuario().equalsIgnoreCase(alias)).count();
        exigir(coincidencias == 1, "La operación inválida creó un duplicado.");
        System.out.println("Duplicado y contador negativo rechazados sin escritura parcial.");

        exigir(dao.desactivar(id), "No se desactivó el usuario.");
        comprobarBaja(buscar(dao, id));
        System.out.println("Baja lógica confirmada. Versión: 2.");
        System.out.println("Usuario de demostración: " + id);
    }

    private static Usuario buscar(UsuarioDao dao, UUID id) throws SQLException {
        return dao.buscar(id).orElseThrow(() -> new IllegalStateException("Usuario no encontrado."));
    }

    private static void comprobarBaja(Usuario usuario) {
        exigir(!usuario.activo() && usuario.version() == 2 && NOMBRE_EDITADO.equals(usuario.nombreVisible()),
                "La edición o la baja lógica no se conservaron.");
    }

    private static void exigirRechazo(String estado, OperacionSql operacion) throws SQLException {
        try {
            operacion.ejecutar();
        } catch (SQLException exception) {
            if (estado.equals(exception.getSQLState())) {
                return;
            }
            throw exception;
        }
        throw new IllegalStateException("PostgreSQL aceptó una operación que debía rechazar.");
    }

    private static void exigir(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new IllegalStateException(mensaje);
        }
    }

    @FunctionalInterface
    private interface OperacionSql {
        void ejecutar() throws SQLException;
    }
}
