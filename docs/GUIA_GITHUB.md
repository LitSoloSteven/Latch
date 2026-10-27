# Guía práctica de Git y GitHub para Latch

Repositorio: [LitSoloSteven/Latch](https://github.com/LitSoloSteven/Latch). Fecha de referencia: 26 de octubre de 2026, zona America/Managua. Esta guía reúne los procedimientos de trabajo del equipo. El estado de la configuración se registra en [CONFIGURACION_GITHUB.md](CONFIGURACION_GITHUB.md).

## 1. Puesta en marcha del equipo

1. **CTardencilla**, **Arisgav** y **wetalff** aceptan la invitación a Latch desde sus cuentas de GitHub. El acceso de escritura queda disponible después de aceptarla.
2. Aris o Caleb revisa la [PR de configuración #2](https://github.com/LitSoloSteven/Latch/pull/2): código, scripts y documentos. La aprobación se realiza desde una cuenta distinta de la del autor, una vez comprobados los checks `ci` y `policy`. Para recibir una solicitud de revisión, primero debe estar aceptada la invitación.
3. La persona que integra utiliza **Squash and merge**, copia íntegro el bloque `Squash message` al mensaje final y comprueba la cabecera y `Refs`. Las reglas de protección se mantienen durante la integración.
4. Después del merge, cada integrante actualiza su copia del proyecto:

   ```powershell
   git switch main
   git pull --ff-only
   ./scripts/install-hooks.ps1
   ```

5. Cada integrante configura su identidad, autenticación de dos factores, JDK 21 y firma de commits con sus propias credenciales.
6. TAR-004 permanece abierta hasta completar los pendientes de construcción reproducible del TRD. La configuración de GitHub y un `verify` sin pruebas no completan esa tarea ni los CP del Plan.

Los formularios, CODEOWNERS y Dependabot estarán disponibles en la rama predeterminada después de integrar la PR. Los archivos presentes solo en una rama de trabajo todavía no constituyen configuración de `main`.

## 2. Preparación de cada computadora

Desde PowerShell:

```powershell
git --version
git clone https://github.com/LitSoloSteven/Latch.git
cd Latch
git config --local user.name "Tu nombre completo"
git config --local user.email "Tu correo verificado en GitHub"
./scripts/install-hooks.ps1
java -version
./mvnw.cmd --version
./mvnw.cmd -B -ntp verify
```

Si `java` no aparece, se selecciona el JDK 21 instalado en IntelliJ o se configura `JAVA_HOME` y se añade su carpeta `bin` a PATH en Windows. Para una sola sesión se puede usar `$env:JAVA_HOME = 'RUTA_REAL_DE_TU_JDK_21'`, sustituyendo la ruta por la instalación local.

Git Credential Manager gestiona el acceso HTTPS mediante el inicio de sesión en el navegador cuando Git lo solicita. Los tokens se conservan fuera de los archivos del proyecto; la contraseña de la cuenta de GitHub no se utiliza como contraseña de Git.

Para la firma SSH, cada integrante crea una clave Ed25519 protegida con passphrase fuera del repositorio y registra **solo su parte pública** como Signing Key en GitHub. Configura `gpg.format=ssh`, `user.signingkey` con la ruta pública real y `commit.gpgsign=true`, y comprueba que GitHub muestre Verified. La verificación local utiliza un archivo `allowed_signers` fuera del repositorio. La firma automática se activa cuando la clave funciona; las claves privadas son personales y no se comparten.

## 3. Ramas que se utilizan

| Rama | Origen | Uso | Plazo propuesto |
| --- | --- | --- | --- |
| `main` | Historial aceptado | Integración mediante PR revisada | Permanente |
| `feature/TAR-nnn-descripcion` | `main` actualizada | Funcionalidad | 5 días |
| `fix/DEF-nnn-descripcion` | `main` actualizada | Defecto vigente | 5 días |
| `docs/TAR-nnn-descripcion` | `main` actualizada | Documentación | 3 días |
| `build/TAR-nnn-descripcion` | `main` actualizada | Construcción y configuración | 3 días |

Los identificadores tienen tres dígitos y las descripciones usan minúsculas ASCII separadas por guiones. El equipo trabaja con una sola rama permanente, sin ramas permanentes por persona. `release/*` y `hotfix/*` siguen siendo una enmienda propuesta de ADR-15; su uso requiere aprobar la enmienda y las versiones requieren aceptación.

## 4. Un cambio de principio a fin

1. El responsable crea o consulta la tarea del Plan en [Issues](https://github.com/LitSoloSteven/Latch/issues). El número de GitHub `#n` y el identificador `TAR-nnn` son diferentes. La tarea incluye requisito/decisión, módulo, responsable, revisor, estimación, precedencias, CP/RA y criterios de aceptación, junto con el milestone del período y las etiquetas aplicables.
2. Con el árbol de trabajo limpio, el autor actualiza `main` y crea la rama de la tarea. Ejemplo para TAR-004:

   ```powershell
   git switch main
   git pull --ff-only
   git switch -c build/TAR-004-ajustar-wrapper
   ```

3. El autor realiza un cambio acotado y ejecuta `./mvnw.cmd -B -ntp verify`. Registra el resultado obtenido y deja pendientes las pruebas manuales que falten por ejecutar.
4. Antes del commit, revisa el diff y agrega los archivos del cambio:

   ```powershell
   git status
   git diff
   git add RUTA_DEL_ARCHIVO_CAMBIADO
   git commit -m "build(M12): ajustar Wrapper (TAR-004)" -m "Refs: TAR-004"
   git push -u origin HEAD
   ```

   `RUTA_DEL_ARCHIVO_CAMBIADO` se sustituye por la ruta correspondiente. Los commits excluyen secretos, preferencias personales y datos operativos. También se admiten `feat`, `fix`, `test` y `docs`. Si se omite el alcance `(M12)`, se añade el pie `Module: M12`. El pie `Refs` debe coincidir con el identificador de la cabecera.

5. El autor abre una PR hacia `main`, completa la plantilla y escribe `Refs #n` con el issue real. El título y la primera línea de `Squash message` deben coincidir. La descripción incluye riesgos, validación y pendientes. El revisor debe ser distinto del autor y de quien realizó el último push.
6. El autor atiende las observaciones. Para actualizar su rama utiliza `git fetch origin` y `git rebase origin/main` únicamente sobre historia propia; puede cancelar con `git rebase --abort`. Si los commits ya están publicados, coordina con el revisor antes de usar `git push --force-with-lease`. Este comando se limita a la rama propia y nunca se aplica a `main`.
7. Con aprobación vigente, conversaciones resueltas y `ci`/`policy` correctos, la persona que integra realiza el squash y comprueba el mensaje final. GitHub elimina la rama de trabajo integrada cuando está habilitada esa opción.
8. Cada integrante actualiza su clon con `git switch main`, `git pull --ff-only` y `git fetch --prune`. La rama local se elimina después de comprobar el merge. Tras squash, `git branch -d NOMBRE` puede rechazarla porque los SHA son distintos; antes de eliminarla se verifica la PR y que no queden cambios por conservar.

## 5. Issues, aceptación y responsabilidades

Los siete formularios cubren TAR, DEF, cambio D44, RSK, ADR, CP y documentación. Los estados son **Preparada, En curso, En revisión, En prueba, Terminada y Bloqueada**. Un issue incompleto queda sin estado asignado hasta el triage. Cada persona mantiene una TAR activa.

Una PR integrada registra integración técnica. Para pasar a En prueba se requieren el paquete ejecutable con etiqueta canónica, CP aplicables y fixture definido. La persona A acepta los resultados antes de Terminada y del cierre manual D47. El registro conserva los resultados fallidos y los pendientes. Las horas reales se completan con el trabajo registrado.

| Cuenta | Responsabilidad del Plan |
| --- | --- |
| `LitSoloSteven` | Coordinación, seguridad y concurrencia; administración de la cuenta propietaria |
| `wetalff` | Datos y persistencia |
| `CTardencilla` | FXML, temas y pruebas |
| `Arisgav` | HTTP, reportes y distribución |

Los colaboradores de este repositorio personal reciben escritura. Las responsabilidades de mantenimiento y triage no son roles separados de la cuenta propietaria. CODEOWNERS propone revisores por rutas; para asignarlos, la invitación debe estar aceptada y la persona debe ser ajena al cambio.

## 6. Hitos, etiquetas y Project

`config/labels.json` conserva 47 etiquetas. `config/milestones.json` contiene seis períodos y diez HIT con las fechas del Plan, convertidas desde el fin del día de Managua a UTC. Crear un milestone no declara entregado su contenido. Una TAR usa el milestone de su período; el seguimiento de un HIT se registra aparte porque un issue admite un solo milestone.

La creación del Project está pendiente porque la credencial HTTPS tiene los alcances `repo`, `workflow` y `gist`, sin acceso a Projects. Steven puede crearlo desde su cuenta en **Your profile → Projects → New project**, con el nombre `Latch - Plan de implementacion`, vincular Latch y configurar estos campos:

| Campo | Tipo y valores |
| --- | --- |
| Status | Selección: Preparada, En curso, En revisión, En prueba, Terminada, Bloqueada |
| TAR | Texto; identificador del catálogo, independiente del número GitHub |
| Módulo | Selección: M01 a M12 y Transversal |
| Horas estimadas / Horas reales | Dos campos numéricos; reales vacío hasta registrar trabajo |
| Sprint | Selección: Sprint 0, Sprint 1, Sprint 2, Sprint 3, Sprint 4, Cierre |
| RA | Texto: RA1 a RA5; justifica No aplica |
| Severidad | Selección: Crítica, Alta, Media, Baja |
| Inicio / Fin | Dos campos de fecha |

El tablero utiliza las vistas Tablero por Status, Tabla por Sprint, Hoja de ruta con Inicio/Fin y Bloqueadas. Se agrega el issue TAR-004 #1 y después las TAR del catálogo, conservando sus responsables y estimaciones originales. El cierre de una TAR requiere aceptación D47; el merge de una PR no activa su cierre automático.

`PROJECT_TOKEN` se conserva fuera del código. La automatización de Projects requiere permisos propios y configuración adicional. El tablero y las invitaciones pendientes se detallan en el registro de configuración.

## 7. Administración y recuperación

El script `scripts/configure-github.ps1` trabaja únicamente sobre `LitSoloSteven/Latch`, usa Git Credential Manager en memoria y no imprime tokens:

```powershell
./scripts/configure-github.ps1 -Action Inspect
```

`Prepare` sincroniza ajustes, etiquetas, milestones e invitaciones. `Protect` aplica los rulesets y exige primero checks `ci` y `policy` correctos de la PR de configuración. Steven administra estos cambios como propietario del repositorio y revisa los JSON en `config/` antes de aplicarlos. La protección se mantiene después de la configuración inicial.

Los hooks ayudan a evitar errores locales; las reglas remotas son las que protegen GitHub. El hook de pre-push rechaza enviar directamente a `main`. El de commit comprueba el mensaje. Cada nuevo clon debe ejecutar `install-hooks.ps1`.

Si `git pull --ff-only` rechaza una divergencia, el autor revisa `git status` y `git log --oneline --graph --all` antes de decidir cómo resolverla. Para deshacer un cambio integrado, prepara una PR con `git revert` y mensaje conforme a la convención. Una reversión de Git no revierte pagos ni migraciones aplicadas. Ante una credencial expuesta, primero se revoca y después se evalúa la limpieza del historial.

Referencias de operación: [GitHub Flow](https://docs.github.com/en/get-started/using-github/github-flow), [rulesets](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-rulesets/about-rulesets), [CODEOWNERS](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/about-code-owners) y la estrategia corregida local. Los documentos originales conservan sus identificadores y fechas.
