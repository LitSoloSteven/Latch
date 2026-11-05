# Demostracion de Persistencia JDBC: Inquilino (#5)

**Modulo / Tarea:** Issue #5 — Tabla `latch.inquilino` y persistencia JDBC  
**Responsable:** Caleb Tardencilla (@CTardencilla)  
**Rama de trabajo:** `caleb`  
**Tecnologias:** Java 21, PostgreSQL, JDBC, Maven, JUnit 5  

---

## 1. Resumen de la Implementacion

En cumplimiento de las especificaciones tecnicas del proyecto Latch y los acuerdos establecidos por el equipo docente:

1. **Script DDL (`latch.inquilino`):**
   - Definido en `src/main/resources/sql/01_crear_latch_inquilino.sql`.
   - Implementa clave primaria `id UUID DEFAULT gen_random_uuid()`.
   - Restriccion `ck_inquilino_version CHECK (version >= 0)`.
   - Clave foranea condicional de auditoria `fk_inquilino_creado_por FOREIGN KEY (creado_por) REFERENCES latch.usuario(id) ON DELETE RESTRICT`.
   - Campos opcionales admiten `NULL` (`identificacion`, `telefono`, `correo`, `contacto_emergencia`, `creado_por`).

2. **Modelo de Dominio (`Inquilino.java`):**
   - Ubicado en `ni.edu.uam.latch.modelo.Inquilino`.
   - Validaciones estrictas:
     - `nombre`: obligatorio, no vacio, maximo 160 caracteres.
     - `identificacion`: maximo 40 caracteres (permite `NULL`).
     - `telefono`: maximo 30 caracteres (permite `NULL`).
     - `correo`: formato RFC 5322 validado cuando se suministra; maximo 254 caracteres (permite `NULL`).
     - `contactoEmergencia`: maximo 200 caracteres (permite `NULL`).
     - `version`: no negativa (`version >= 0`).
     - `activo`: boolean con valor predeterminado `true`.

3. **Acceso a Datos JDBC (`InquilinoDao.java`):**
   - Ubicado en `ni.edu.uam.latch.dao.InquilinoDao`.
   - Consultas parametrizadas completas contra `latch.inquilino`.
   - Cierre de conexiones, declaraciones y cursores mediante `try-with-resources`.
   - Operaciones implementadas:
     - `insertar(Inquilino inquilino)`: alta de inquilino.
     - `buscarPorId(UUID id)`: recuperacion por clave primaria.
     - `listarTodos()` y `listarActivos()`: listado general y filtrado por estado.
     - `actualizarContacto(UUID id, String tel, String correo, String emergencia, long ver)`: actualizacion parcial e incremento optimista de version.
     - `actualizar(Inquilino inquilino)`: actualizacion completa e incremento de version.
     - `darDeBaja(UUID id, long ver)`: baja logica (`activo = false`) sin borrado fisico.

4. **Fabrica de Conexiones Segura (`ConexionBD.java`):**
   - Ubicada en `ni.edu.uam.latch.dao.ConexionBD`.
   - Lectura de variables de entorno externas: `DB_URL`, `DB_USER` y `DB_PASSWORD`.
   - Sanitizacion automatica de URLs y mensajes de error para nunca exponer credenciales secretas en registros ni excepciones.

5. **Datos Sinteticos (`02_datos_sinteticos_inquilino.sql`):**
   - Inquilino 1: Registro completo (`Carlos Alberto Mendoza Ruiz`).
   - Inquilino 2: Registro completo alternativo (`Lucia Sofia Morales Gomez`).
   - Inquilino 3: Registro con campos opcionales nulos sin telefono ni correo (`Marvin Jose Castillo Blandon`).

---

## 2. Instrucciones de Ejecucion y Validacion

### 2.1. Validacion de Compilacion y Pruebas Unitarias
Ejecutar desde PowerShell en la raiz del proyecto:

```powershell
$env:JAVA_HOME = "C:\Users\caleb\.jdks\temurin-21.0.12.1"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
./mvnw.cmd -B -ntp verify
```

**Resultado obtenido:**
```text
[INFO] Running ni.edu.uam.latch.modelo.InquilinoTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### 2.2. Ejecucion de la Demostracion de Persistencia
Configurar las variables de entorno de la base de datos PostgreSQL de prueba y ejecutar el punto de entrada:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/latch"
$env:DB_USER = "tu_usuario"
$env:DB_PASSWORD = "tu_password"

java -cp "target/classes;target/latch-0.1.0.jar;C:\Users\caleb\.m2\repository\org\postgresql\postgresql\42.7.2\postgresql-42.7.2.jar" ni.edu.uam.latch.DemoInquilino
```

En caso de que las credenciales no esten configuradas o el servicio este inaccesible, el sistema informa el error de conexion de manera segura sin exponer la contrasena y demuestra la proteccion de integridad en memoria.

---

## 3. Comprobacion Directa en PostgreSQL (SELECT)

Para comprobar el ciclo de vida directamente en PostgreSQL via `psql`:

```sql
-- 1. Consultar todos los registros
SELECT id, nombre, identificacion, telefono, correo, activo, version 
FROM latch.inquilino;

-- 2. Comprobar inquilino con campos nulos
SELECT id, nombre, telefono, correo 
FROM latch.inquilino 
WHERE telefono IS NULL AND correo IS NULL;

-- 3. Comprobar baja logica (permanece el registro con activo=false)
SELECT id, nombre, activo, version 
FROM latch.inquilino 
WHERE activo = FALSE;
```
