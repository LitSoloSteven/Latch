# Entrega de base de datos

Usa una base de demostración de PostgreSQL independiente. Desde una cuenta administradora de PostgreSQL crea un rol de acceso con contraseña propia y una base de la que sea dueño. Por ejemplo, en `psql`:

```sql
CREATE ROLE latch_demo LOGIN;
\password latch_demo
CREATE DATABASE latch_demo OWNER latch_demo;
```

Desde la raíz del proyecto, abre PowerShell con JDK 21 en PATH y configura el acceso en la sesión actual. La contraseña se solicita de forma interactiva.

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5432/latch_demo'
$env:DB_USER = 'latch_demo'
$env:DB_PASSWORD = Read-Host 'Contraseña de PostgreSQL' -MaskInput
./mvnw.cmd -B -ntp verify

$driver = Join-Path $env:USERPROFILE '.m2/repository/org/postgresql/postgresql/42.7.14/postgresql-42.7.14.jar'
$classpath = "target/classes;$driver"
java -cp $classpath ni.edu.uam.latch.db.ConnectionCheck
```

La comprobación debe imprimir `PostgreSQL: SELECT 1 -> 1`. Si la configuración falta o la conexión falla, el comando devuelve código 1 sin mostrar la URL, el usuario ni la contraseña.

## Usuarios y roles

En una base de demostración limpia, aplica los scripts en orden. `psql` solicita la contraseña y se detiene ante el primer error. Ajusta el host, puerto y base a tu configuración local.

```powershell
psql -h localhost -p 5432 -U $env:DB_USER -d latch_demo -W -v ON_ERROR_STOP=1 -f db/001-usuarios.sql -f db/002-usuarios-demo.sql
java -cp $classpath ni.edu.uam.latch.usuario.UsuarioDemo demostrar
```

El primer script crea las tablas y los roles. El segundo carga el usuario sintético con UUID `d4030000-0000-4000-8000-000000000001`, que sirve como referencia `creado_por` para otras tablas de esta demostración. Su hash Argon2id corresponde a un valor aleatorio de prueba.

La demostración comprueba el alta, la consulta por ID, el listado, el cambio del nombre visible, el rechazo de un duplicado y de un contador negativo, y la baja lógica. Al finalizar imprime el UUID del usuario creado. Copia ese UUID y arranca un segundo proceso para verificar que sigue persistido.

```powershell
$id = [guid](Read-Host 'UUID mostrado por la demostración')
java -cp $classpath ni.edu.uam.latch.usuario.UsuarioDemo verificar $id
psql -h localhost -p 5432 -U $env:DB_USER -d latch_demo -W -c "SELECT id, nombre_visible, activo, version FROM latch.usuario WHERE id = '$id';"
```

El usuario debe conservar el nombre actualizado, `activo = false` y `version = 2`.

## Propiedades

Después de cargar usuarios y el usuario sintético, crea la tabla y carga una propiedad de prueba. Usa el mismo acceso y `$classpath` de los pasos anteriores.

```powershell
psql -h localhost -p 5432 -U $env:DB_USER -d latch_demo -W -v ON_ERROR_STOP=1 -f db/005-propiedades.sql -f db/006-propiedades-demo.sql
java -cp $classpath ni.edu.uam.latch.propiedad.PropiedadDemo demostrar
$id = [guid](Read-Host 'UUID mostrado por la demostración')
java -cp $classpath ni.edu.uam.latch.propiedad.PropiedadDemo verificar $id
psql -h localhost -p 5432 -U $env:DB_USER -d latch_demo -W -c "SELECT id, nombre, direccion, activa, version FROM latch.propiedad WHERE id = '$id';"
```

La demostración crea un registro distinto de la propiedad que usará Unidades. Verifica consulta, listado, edición, baja lógica y rechazo de una dirección vacía y de un usuario de auditoría inexistente. El registro desactivado debe conservar el nombre y la dirección editados con `activa = false` y `version = 2`.

La propiedad activa que puede referenciar Unidades tiene UUID `d4040000-0000-4000-8000-000000000001`. Para la prueba de conexión inválida, repite `verificar` apuntando `DB_URL` a una base inexistente y comprueba el código 1.
