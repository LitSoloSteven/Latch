package ni.edu.uam.latch.propiedad;

import ni.edu.uam.latch.db.Database;

import java.sql.SQLException;
import java.util.UUID;

public final class PropiedadDemo {
    private static final UUID ACTOR = UUID.fromString("d4030000-0000-4000-8000-000000000001");
    private static final UUID PROPIEDAD_PARA_UNIDADES = UUID.fromString("d4040000-0000-4000-8000-000000000001");
    private static final String NOMBRE_EDITADO = "Propiedad actualizada";
    private static final String DIRECCION_EDITADA = "Dirección sintética actualizada";

    private PropiedadDemo() {
    }

    public static void main(String[] args) {
        try {
            var dao = new PropiedadDao(Database.fromEnvironment());
            if (args.length == 1 && "demostrar".equals(args[0])) {
                demostrar(dao);
            } else if (args.length == 2 && "verificar".equals(args[0])) {
                UUID id = UUID.fromString(args[1]);
                verificarBaja(dao, id);
                System.out.println("Persistencia tras reinicio confirmada: " + id);
            } else {
                throw new IllegalArgumentException();
            }
        } catch (SQLException exception) {
            System.err.println("No se pudo completar la demostración en PostgreSQL.");
            System.exit(1);
        } catch (IllegalArgumentException exception) {
            System.err.println("Revisa DB_URL, DB_USER y DB_PASSWORD. Usa demostrar o verificar <UUID>.");
            System.exit(1);
        } catch (IllegalStateException exception) {
            System.err.println(exception.getMessage());
            System.exit(1);
        }
    }

    private static void demostrar(PropiedadDao dao) throws SQLException {
        Propiedad semilla = buscar(dao, PROPIEDAD_PARA_UNIDADES);
        comprobar(semilla.activa() && ACTOR.equals(semilla.creadoPor()), "La propiedad de prueba para unidades es inválida.");

        UUID id = dao.crear("Propiedad sintética", "Dirección sintética", ACTOR);
        Propiedad creada = buscar(dao, id);
        comprobar(creada.activa() && creada.version() == 0 && ACTOR.equals(creada.creadoPor())
                && "Propiedad sintética".equals(creada.nombre()) && "Dirección sintética".equals(creada.direccion()),
                "El alta no conservó los datos.");
        comprobar(dao.listar().contains(creada), "La propiedad no aparece en el listado.");
        comprobar(dao.actualizar(id, NOMBRE_EDITADO, DIRECCION_EDITADA, 0), "Falló la edición.");
        Propiedad editada = buscar(dao, id);
        comprobar(editada.version() == 1 && editada.activa()
                && NOMBRE_EDITADO.equals(editada.nombre()) && DIRECCION_EDITADA.equals(editada.direccion()),
                "No se conservó la edición.");
        comprobar(!dao.actualizar(id, "Versión antigua", DIRECCION_EDITADA, 0), "Se aceptó una versión obsoleta.");
        verificarRechazos(dao, editada);
        comprobar(dao.desactivar(id, 1), "Falló la baja lógica.");
        verificarBaja(dao, id);
        System.out.println("Alta, consulta, listado, edición, rechazo y baja lógica confirmados.");
        System.out.println("Propiedad de demostración: " + id);
    }

    private static void verificarRechazos(PropiedadDao dao, Propiedad editada) throws SQLException {
        try {
            dao.actualizar(editada.id(), "No debe persistir", "  ", editada.version());
            throw new IllegalStateException("Se aceptó una dirección vacía.");
        } catch (IllegalArgumentException exception) {
            comprobar(editada.equals(buscar(dao, editada.id())), "La edición inválida cambió la propiedad.");
        }

        String nombreInvalido = "Auditoría inválida " + editada.id();
        try {
            dao.crear(nombreInvalido, "Dirección sintética", UUID.randomUUID());
            throw new IllegalStateException("Se aceptó un usuario de auditoría inexistente.");
        } catch (SQLException exception) {
            if (!"23503".equals(exception.getSQLState())) {
                throw exception;
            }
            comprobar(dao.listar().stream().noneMatch(p -> nombreInvalido.equals(p.nombre())),
                    "El alta inválida dejó un registro.");
        }
    }

    private static Propiedad buscar(PropiedadDao dao, UUID id) throws SQLException {
        return dao.buscar(id).orElseThrow(() -> new IllegalStateException("Propiedad no encontrada."));
    }

    private static void verificarBaja(PropiedadDao dao, UUID id) throws SQLException {
        Propiedad p = buscar(dao, id);
        comprobar(!p.activa() && p.version() == 2
                && NOMBRE_EDITADO.equals(p.nombre()) && DIRECCION_EDITADA.equals(p.direccion()),
                "No se conservaron la edición y la baja lógica.");
    }

    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new IllegalStateException(mensaje);
        }
    }
}
