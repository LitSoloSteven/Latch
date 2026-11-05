package ni.edu.uam.latch;

import ni.edu.uam.latch.dao.InquilinoDao;
import ni.edu.uam.latch.db.Database;
import ni.edu.uam.latch.modelo.Inquilino;

import java.sql.SQLException;
import java.util.UUID;

public final class DemoInquilino {
    private static final UUID ACTOR_DEMO = UUID.fromString("d4030000-0000-4000-8000-000000000001");
    private static final String CORREO = "contacto@example.test";

    private DemoInquilino() {
    }

    public static void main(String[] args) {
        try {
            var dao = new InquilinoDao(Database.fromEnvironment()::open);
            if (args.length == 2 && "verificar".equals(args[0])) {
                verificarBaja(dao, UUID.fromString(args[1]));
                System.out.println("Persistencia tras reinicio confirmada: " + args[1]);
            } else if (args.length == 1 && "demostrar".equals(args[0])) {
                demostrar(dao);
            } else {
                throw new IllegalArgumentException();
            }
        } catch (SQLException ex) {
            System.err.println("No se pudo completar la demostración en PostgreSQL.");
            System.exit(1);
        } catch (IllegalArgumentException ex) {
            System.err.println("Revisa DB_URL, DB_USER y DB_PASSWORD. Usa demostrar o verificar <UUID>.");
            System.exit(1);
        } catch (IllegalStateException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }

    private static void demostrar(InquilinoDao dao) throws SQLException {
        var nuevo = dao.insertar(new Inquilino("Inquilino de demostración", null, null, null, null, ACTOR_DEMO));
        UUID id = nuevo.getId();
        var recuperado = buscar(dao, id);
        comprobar(recuperado.getTelefono() == null && recuperado.getCorreo() == null
                && ACTOR_DEMO.equals(recuperado.getCreadoPor()), "El alta no conservó los datos.");
        comprobar(dao.listarTodos().stream().anyMatch(i -> id.equals(i.getId())), "Falta el registro en el listado.");

        comprobar(dao.actualizarContacto(id, "+505 0000-0000", CORREO, null, 0), "No se actualizó el contacto.");
        var editado = buscar(dao, id);
        comprobar(editado.getVersion() == 1 && CORREO.equals(editado.getCorreo()), "No se conservó el contacto.");
        comprobar(!dao.actualizarContacto(id, null, null, null, 0), "Se aceptó una versión obsoleta.");

        int antes = dao.listarTodos().size();
        try {
            dao.insertar(new Inquilino("   ", null, null, null, null, ACTOR_DEMO));
            throw new IllegalStateException("Se aceptó un nombre vacío.");
        } catch (IllegalArgumentException ex) {
            comprobar(dao.listarTodos().size() == antes, "El alta inválida cambió la tabla.");
        }

        comprobar(dao.darDeBaja(id, 1), "No se desactivó el registro.");
        verificarBaja(dao, id);
        System.out.println("Alta, consulta, listado, edición, rechazo y baja lógica confirmados.");
        System.out.println("Inquilino de demostración: " + id);
    }

    private static Inquilino buscar(InquilinoDao dao, UUID id) throws SQLException {
        return dao.buscarPorId(id).orElseThrow(() -> new IllegalStateException("Inquilino no encontrado."));
    }

    private static void verificarBaja(InquilinoDao dao, UUID id) throws SQLException {
        var registro = buscar(dao, id);
        comprobar(!registro.isActivo() && registro.getVersion() == 2 && CORREO.equals(registro.getCorreo()),
                "No se conservó la edición o la baja lógica.");
    }

    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new IllegalStateException(mensaje);
        }
    }
}
