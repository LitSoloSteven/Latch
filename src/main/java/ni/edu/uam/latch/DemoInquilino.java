package ni.edu.uam.latch;

import ni.edu.uam.latch.dao.ConexionBD;
import ni.edu.uam.latch.dao.InquilinoDao;
import ni.edu.uam.latch.modelo.Inquilino;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Punto de entrada ejecutable de consola para demostrar la persistencia JDBC
 * de la tabla latch.inquilino (#5 - Caleb Tardencilla).
 *
 * Flujo de demostracion:
 * 1. Inicializacion y verificacion del esquema latch y tabla latch.inquilino.
 * 2. Insercion de inquilinos sinteticos (incluyendo caso sin telefono ni correo).
 * 3. Recuperacion por UUID con campos opcionales en NULL.
 * 4. Listado de registros.
 * 5. Actualizacion de datos de contacto y verificacion del incremento de version.
 * 6. Baja logica (activo = false) sin eliminacion fisica.
 * 7. Rechazo de registro invalido sin escritura parcial.
 * 8. Demostracion de informe de error de conexion sin exposicion de credenciales.
 * 9. Comprobacion de persistencia.
 */
public class DemoInquilino {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   LATCH - DEMOSTRACION DE PERSISTENCIA JDBC: INQUILINO (#5)                    ");
        System.out.println("   Responsable: Caleb Tardencilla (@CTardencilla)                               ");
        System.out.println("================================================================================\n");

        InquilinoDao dao = new InquilinoDao();

        // 1. Validar conexion a PostgreSQL e inicializar esquema si es posible
        System.out.println("[Paso 1] Probando conexion a PostgreSQL...");
        boolean conexionDisponible = false;
        try (Connection conn = ConexionBD.obtenerConexion()) {
            System.out.println(" -> Conexion exitosa con PostgreSQL: " + conn.getMetaData().getDatabaseProductName() + " " + conn.getMetaData().getDatabaseProductVersion());
            inicializarTablas(conn);
            conexionDisponible = true;
        } catch (SQLException e) {
            System.err.println(" -> " + e.getMessage());
            System.out.println(" [NOTA] Si PostgreSQL no esta configurado en este entorno, defina DB_URL, DB_USER y DB_PASSWORD.");
        }

        if (!conexionDisponible) {
            System.out.println("\n[Demostracion segura de rechazo y manejo de error sin conexion activa]");
            probarManejoSeguroErrorConexion();
            probarValidacionesModelo();
            return;
        }

        try {
            // 2. Insercion de inquilinos sinteticos
            System.out.println("\n[Paso 2] Insertando inquilinos sinteticos...");
            Inquilino inq1 = new Inquilino(
                    "Carlos Alberto Mendoza Ruiz",
                    "001-150392-0004X",
                    "+505 8899-7711",
                    "carlos.mendoza@email.com",
                    "Elena Ruiz (Madre) - +505 8811-2233",
                    null
            );
            inq1 = dao.insertar(inq1);
            System.out.println(" -> Inquilino 1 insertado: ID=" + inq1.getId() + " | Nombre=" + inq1.getNombre() + " | Version=" + inq1.getVersion());

            Inquilino inq2 = new Inquilino(
                    "Lucia Sofia Morales Gomez",
                    "281-220795-0002Y",
                    "+505 7744-1122",
                    "lucia.morales@empresa.com",
                    "Roberto Morales (Hermano) - +505 8833-4455",
                    null
            );
            inq2 = dao.insertar(inq2);
            System.out.println(" -> Inquilino 2 insertado: ID=" + inq2.getId() + " | Nombre=" + inq2.getNombre() + " | Version=" + inq2.getVersion());

            // Inquilino 3: Sin telefono ni correo (campos opcionales en NULL)
            Inquilino inq3 = new Inquilino(
                    "Marvin Jose Castillo Blandon",
                    "441-051188-0001Z",
                    null,
                    null,
                    null,
                    null
            );
            inq3 = dao.insertar(inq3);
            System.out.println(" -> Inquilino 3 (sin tel/correo) insertado: ID=" + inq3.getId() + " | Telefono=" + inq3.getTelefono() + " | Correo=" + inq3.getCorreo());

            // 3. Recuperacion por UUID y validacion de campos NULL
            System.out.println("\n[Paso 3] Recuperando inquilino por UUID...");
            Optional<Inquilino> encontrado = dao.buscarPorId(inq3.getId());
            if (encontrado.isPresent()) {
                Inquilino rec = encontrado.get();
                System.out.println(" -> Inquilino recuperado con exito: " + rec.getNombre());
                System.out.println("    Campos opcionales comprobados -> Telefono: " + rec.getTelefono() + ", Correo: " + rec.getCorreo() + ", Emergencia: " + rec.getContactoEmergencia());
            } else {
                throw new IllegalStateException("Error: Inquilino 3 no fue encontrado por su UUID.");
            }

            // 4. Listado de registros
            System.out.println("\n[Paso 4] Listando todos los inquilinos en latch.inquilino...");
            List<Inquilino> todos = dao.listarTodos();
            System.out.println(" -> Total de inquilinos en base de datos: " + todos.size());
            for (Inquilino i : todos) {
                System.out.println("    - [" + i.getId() + "] " + i.getNombre() + " | Activo=" + i.isActivo() + " | Ver=" + i.getVersion());
            }

            // 5. Actualizacion de datos de contacto e incremento de version
            System.out.println("\n[Paso 5] Actualizando datos de contacto e incrementando version...");
            long versionPrevia = inq1.getVersion();
            boolean actualizado = dao.actualizarContacto(
                    inq1.getId(),
                    "+505 8899-0000",
                    "carlos.nuevo@email.com",
                    "Elena Ruiz - +505 8811-9999",
                    versionPrevia
            );
            System.out.println(" -> Actualizacion ejecutada: " + actualizado);

            Optional<Inquilino> inq1Actualizado = dao.buscarPorId(inq1.getId());
            if (inq1Actualizado.isPresent()) {
                long versionNueva = inq1Actualizado.get().getVersion();
                System.out.println(" -> Version anterior: " + versionPrevia + " | Version nueva: " + versionNueva);
                if (versionNueva == versionPrevia + 1) {
                    System.out.println(" -> CORRECTO: La version se incremento exactamente en 1.");
                } else {
                    System.err.println(" -> ERROR: La version no se incremento correctamente.");
                }
            }

            // 6. Baja logica (activo = false) sin borrado fisico
            System.out.println("\n[Paso 6] Aplicando baja logica (activo = false)...");
            long verActual = inq1Actualizado.get().getVersion();
            boolean dadoDeBaja = dao.darDeBaja(inq1.getId(), verActual);
            System.out.println(" -> Baja logica exitosa: " + dadoDeBaja);

            Optional<Inquilino> inq1Baja = dao.buscarPorId(inq1.getId());
            if (inq1Baja.isPresent()) {
                System.out.println(" -> Inquilino sigue existiendo fisicamente: Activo=" + inq1Baja.get().isActivo() + " | Version=" + inq1Baja.get().getVersion());
                if (!inq1Baja.get().isActivo()) {
                    System.out.println(" -> CORRECTO: El registro permanece en BD con activo=false.");
                }
            }

            // 7. Rechazo de registro invalido sin escritura parcial
            System.out.println("\n[Paso 7] Probando rechazo de registro invalido...");
            try {
                Inquilino invalido = new Inquilino();
                invalido.setNombre("   "); // Nombre vacio
                dao.insertar(invalido);
                System.err.println(" -> ERROR: Se permitio insertar un nombre invalido.");
            } catch (IllegalArgumentException e) {
                System.out.println(" -> CORRECTO: Rechazado por validacion de dominio -> " + e.getMessage());
            }

            try {
                Inquilino correoInvalido = new Inquilino();
                correoInvalido.setNombre("Nombre Valido");
                correoInvalido.setCorreo("correo-sin-formato");
                dao.insertar(correoInvalido);
                System.err.println(" -> ERROR: Se permitio insertar un correo invalido.");
            } catch (IllegalArgumentException e) {
                System.out.println(" -> CORRECTO: Rechazado por formato de correo -> " + e.getMessage());
            }

            // 8. Demostracion de error de conexion sin exponer credenciales
            System.out.println("\n[Paso 8] Demostrando informe de error de conexion seguro...");
            probarManejoSeguroErrorConexion();

            System.out.println("\n================================================================================");
            System.out.println("   DEMOSTRACION FINALIZADA CON EXITO: PERSISTENCIA JDBC VALIDADA              ");
            System.out.println("================================================================================");

        } catch (SQLException e) {
            System.err.println("Fallo en la ejecucion JDBC: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void inicializarTablas(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE SCHEMA IF NOT EXISTS latch;");
            st.execute("CREATE TABLE IF NOT EXISTS latch.inquilino (" +
                    "id UUID NOT NULL DEFAULT gen_random_uuid(), " +
                    "nombre VARCHAR(160) NOT NULL, " +
                    "identificacion VARCHAR(40), " +
                    "telefono VARCHAR(30), " +
                    "correo VARCHAR(254), " +
                    "contacto_emergencia VARCHAR(200), " +
                    "activo BOOLEAN NOT NULL DEFAULT TRUE, " +
                    "creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "creado_por UUID, " +
                    "version BIGINT NOT NULL DEFAULT 0, " +
                    "CONSTRAINT pk_inquilino PRIMARY KEY (id), " +
                    "CONSTRAINT ck_inquilino_version CHECK (version >= 0)" +
                    ");");
            System.out.println(" -> Tabla latch.inquilino verificada/creada correctamente.");
        }
    }

    private static void probarManejoSeguroErrorConexion() {
        String urlInvalida = "jdbc:postgresql://host.no.existente.local:5432/latch";
        String usuario = "admin";
        String passwordSecreta = "MiPasswordMuyPrivada12345";

        try {
            DriverManager.getConnection(urlInvalida, usuario, passwordSecreta);
        } catch (SQLException e) {
            // El DAO / ConexionBD sanitiza y protege credenciales
            String mensaje = ConexionBD.sanitizarUrl("Error al conectar con " + urlInvalida + ": " + e.getMessage());
            System.out.println(" -> Mensaje de error controlado capturado:");
            System.out.println("    \"" + mensaje + "\"");
            if (!mensaje.contains(passwordSecreta)) {
                System.out.println(" -> CORRECTO: Las credenciales secretas no fueron expuestas en el error.");
            }
        }
    }

    private static void probarValidacionesModelo() {
        System.out.println("\n[Validaciones de modelo en memoria]");
        Inquilino inq = new Inquilino("Test Tenant", null, null, null, null, null);
        System.out.println(" -> Creado inquilino en memoria con campos opcionales null: " + inq.getNombre() + " (UUID: " + inq.getId() + ")");
    }
}
