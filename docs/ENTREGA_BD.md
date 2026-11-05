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
