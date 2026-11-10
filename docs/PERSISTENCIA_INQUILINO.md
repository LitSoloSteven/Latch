# Persistencia de inquilinos

El modelo, el DAO y las pruebas de dominio provienen del trabajo de Caleb Tardencilla en la PR #16. El DAO usa la fábrica JDBC compartida. La migración requiere la tabla de usuarios y sus datos sintéticos para verificar la clave foránea de auditoría.

Usa una base PostgreSQL desechable y configura `DB_URL`, `DB_USER` y `DB_PASSWORD` siguiendo `docs/ENTREGA_BD.md`. Con los scripts de usuarios de #14 disponibles, ejecuta desde la raíz del repositorio:

```powershell
psql -h localhost -p 5432 -U $env:DB_USER -d latch_demo -W -v ON_ERROR_STOP=1 -f db/001-usuarios.sql -f db/002-usuarios-demo.sql -f db/003-inquilinos.sql -f db/004-inquilinos-demo.sql
./mvnw.cmd -B -ntp verify
$driver = Join-Path $env:USERPROFILE '.m2/repository/org/postgresql/postgresql/42.7.14/postgresql-42.7.14.jar'
$classpath = "target/classes;$driver"
java -cp $classpath ni.edu.uam.latch.DemoInquilino demostrar
$id = [guid](Read-Host 'UUID del inquilino de demostración')
java -cp $classpath ni.edu.uam.latch.DemoInquilino verificar $id
psql -h localhost -p 5432 -U $env:DB_USER -d latch_demo -W -c "SELECT id, activo, version, creado_por FROM latch.inquilino WHERE id = '$id';"
```

El registro debe conservar el contacto actualizado, `activo = false` y `version = 2`. Para comprobar el fallo de conexión, repite el comando `verificar` con una base inexistente en `DB_URL`. El proceso debe devolver código 1 sin imprimir credenciales.
