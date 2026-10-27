# Guía práctica de Git y GitHub para Latch

Repositorio: [LitSoloSteven/Latch](https://github.com/LitSoloSteven/Latch). Configuración preparada el 26 de octubre de 2026, zona America/Managua. El estado comprobado y las limitaciones se registran en [CONFIGURACION_GITHUB.md](CONFIGURACION_GITHUB.md).

## 1. Qué debes hacer tú primero

1. Pide a **CTardencilla**, **Arisgav** y **wetalff** que acepten sus invitaciones a Latch desde su cuenta de GitHub. Una invitación pendiente todavía no concede acceso efectivo.
2. Pide a Aris o Caleb que revise la [PR de configuración #2](https://github.com/LitSoloSteven/Latch/pull/2) desde su propia cuenta. Debe revisar el código, los scripts y los documentos, esperar los checks `ci` y `policy`, y aprobar si corresponde. El autor no puede aportar la aprobación ajena. GitHub no permite solicitarles revisión mientras la invitación siga pendiente.
3. Cuando GitHub permita integrar, usa **Squash and merge**. Copia íntegro el bloque `Squash message` de la PR al mensaje final; comprueba cabecera y `Refs`. No elimines las reglas para evitar la revisión.
4. Después del merge, ejecuta en este proyecto:

   ```powershell
   git switch main
   git pull --ff-only
   ./scripts/install-hooks.ps1
   ```

5. Cada integrante configura su identidad, autenticación de dos factores, JDK 21 y firma de commits. Las credenciales, la aceptación de invitaciones y una revisión personal corresponden a sus titulares.
6. Mantén abierta TAR-004 hasta completar los pendientes de construcción reproducible del TRD. La configuración de GitHub y un `verify` sin pruebas no completan esa tarea ni los CP del Plan.

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

Si `java` no aparece, selecciona el JDK 21 instalado en IntelliJ o configura `JAVA_HOME` y añade su carpeta `bin` a PATH en Windows. Para una sola sesión puedes usar `$env:JAVA_HOME = 'RUTA_REAL_DE_TU_JDK_21'`; sustituye la ruta antes de ejecutar. No copies rutas de otra computadora.

Git Credential Manager gestiona el acceso HTTPS. Inicia sesión en el navegador cuando Git lo solicite. No uses la contraseña de GitHub como contraseña de Git ni guardes tokens en archivos del proyecto. La conexión del plugin GitHub y la autenticación de la terminal son accesos independientes.

Para firma SSH, crea tu propia clave Ed25519 protegida con passphrase fuera del repositorio y registra **solo su parte pública** como Signing Key en GitHub. Configura `gpg.format=ssh`, `user.signingkey` con la ruta pública real y `commit.gpgsign=true`; prueba que GitHub muestre Verified. Prepara `allowed_signers` fuera del repositorio para verificar localmente. No actives firma automática antes de tener una clave funcional. La configuración inicial no crea ni comparte claves privadas.

## 3. Ramas que se utilizan

| Rama | Origen | Uso | Plazo propuesto |
| --- | --- | --- | --- |
| `main` | Historial aceptado | Integración mediante PR revisada | Permanente |
| `feature/TAR-nnn-descripcion` | `main` actualizada | Funcionalidad | 5 días |
| `fix/DEF-nnn-descripcion` | `main` actualizada | Defecto vigente | 5 días |
| `docs/TAR-nnn-descripcion` | `main` actualizada | Documentación | 3 días |
| `build/TAR-nnn-descripcion` | `main` actualizada | Construcción y configuración | 3 días |

Los identificadores tienen tres dígitos. Usa descripciones en minúsculas ASCII separadas por guiones. No crees ramas permanentes por persona. `release/*` y `hotfix/*` siguen siendo una enmienda propuesta de ADR-15; no se habilitan por anticipado ni se crean versiones sin aceptación.

## 4. Un cambio de principio a fin

1. En [Issues](https://github.com/LitSoloSteven/Latch/issues), crea o consulta la tarea del Plan. El número de GitHub `#n` y el identificador `TAR-nnn` son diferentes. Registra requisito/decisión, módulo, responsable, revisor, estimación, precedencias, CP/RA y aceptación. Selecciona el milestone del período y las etiquetas aplicables.
2. Actualiza `main` con el árbol limpio y crea la rama propia. Ejemplo de sintaxis para la tarea existente TAR-004:

   ```powershell
   git switch main
   git pull --ff-only
   git switch -c build/TAR-004-ajustar-wrapper
   ```

3. Haz un cambio acotado y ejecuta `./mvnw.cmd -B -ntp verify`. Registra el resultado real; no marques pruebas manuales que no ejecutaste.
4. Revisa y registra los archivos concretos:

   ```powershell
   git status
   git diff
   git add RUTA_DEL_ARCHIVO_CAMBIADO
   git commit -m "build(M12): ajustar Wrapper (TAR-004)" -m "Refs: TAR-004"
   git push -u origin HEAD
   ```

   Sustituye `RUTA_DEL_ARCHIVO_CAMBIADO`. No agregues secretos, preferencias personales ni datos operativos. También se admiten `feat`, `fix`, `test` y `docs`. Sin alcance `(M12)`, añade el pie `Module: M12`. El pie `Refs` debe coincidir con el identificador de la cabecera.

5. Abre una PR hacia `main`. Completa la plantilla y `Refs #n` con el issue real; su título y la primera línea de `Squash message` deben coincidir. Indica riesgos, validación y pendientes. Solicita revisión de una persona distinta del autor y del último push.
6. Atiende las observaciones. Si necesitas actualizar tu rama, usa `git fetch origin` y `git rebase origin/main` únicamente sobre historia propia. Si hay dudas, `git rebase --abort`. Si ya publicaste esos commits, coordina con el revisor antes de `git push --force-with-lease`; se limita a tu rama, nunca a `main`.
7. Con aprobación vigente, conversaciones resueltas y `ci`/`policy` correctos, integra por squash y comprueba el mensaje final. GitHub elimina la rama de trabajo integrada cuando está habilitada esa opción.
8. Actualiza tu clon: `git switch main`, `git pull --ff-only` y `git fetch --prune`. Elimina la rama local solo después de comprobar el merge. Tras squash, `git branch -d NOMBRE` puede rechazarla porque los SHA son distintos: verifica la PR y el árbol antes de decidir eliminarla; no uses borrado forzado a ciegas.

## 5. Issues, aceptación y responsabilidades

Los siete formularios cubren TAR, DEF, cambio D44, RSK, ADR, CP y documentación. Los estados son **Preparada, En curso, En revisión, En prueba, Terminada y Bloqueada**. Un issue incompleto queda sin estado asignado hasta el triage. Cada persona mantiene una TAR activa.

Una PR integrada registra integración técnica. Para pasar a En prueba se requieren el paquete ejecutable con etiqueta canónica, CP aplicables y fixture definido. La persona A acepta los resultados antes de Terminada y del cierre manual D47. Conserva resultados fallidos y pendientes. Las horas reales solo se completan con trabajo registrado.

| Cuenta | Responsabilidad del Plan |
| --- | --- |
| `LitSoloSteven` | Coordinación, seguridad y concurrencia; administración de la cuenta propietaria |
| `wetalff` | Datos y persistencia |
| `CTardencilla` | FXML, temas y pruebas |
| `Arisgav` | HTTP, reportes y distribución |

Los colaboradores de este repositorio personal reciben escritura. Las responsabilidades de mantenimiento y triage no son roles separados de la cuenta propietaria. CODEOWNERS propone revisores por rutas; revisa que la invitación esté aceptada y que el revisor sea ajeno al cambio.

## 6. Hitos, etiquetas y Project

`config/labels.json` conserva 47 etiquetas. `config/milestones.json` contiene seis períodos y diez HIT con las fechas del Plan, convertidas desde el fin del día de Managua a UTC. Crear un milestone no declara entregado su contenido. Una TAR usa el milestone de su período; el seguimiento de un HIT se registra aparte porque un issue admite un solo milestone.

El Project no pudo crearse: la credencial HTTPS tiene `repo`, `workflow` y `gist`, sin acceso a Projects. El control de Edge también se detuvo al no poder verificar la URL. Crea el tablero desde tu cuenta: **Your profile → Projects → New project**. Nómbralo `Latch - Plan de implementacion`, vincula Latch y configura estos campos:

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

Guarda las vistas Tablero por Status, Tabla por Sprint, Hoja de ruta con Inicio/Fin y Bloqueadas. Agrega el issue TAR-004 #1 y después las TAR del catálogo, conservando sus responsables y estimaciones originales. No actives automatización que cierre una TAR por el mero merge de una PR.

No guardes `PROJECT_TOKEN` en código. La automatización de Projects requiere permisos propios y configuración adicional; la creación de YAML no los concede. El tablero y las invitaciones pendientes se detallan en el registro de configuración.

## 7. Administración y recuperación

El script `scripts/configure-github.ps1` trabaja únicamente sobre `LitSoloSteven/Latch`, usa Git Credential Manager en memoria y no imprime tokens:

```powershell
./scripts/configure-github.ps1 -Action Inspect
```

`Prepare` sincroniza ajustes, etiquetas, milestones e invitaciones. `Protect` aplica los rulesets y exige primero checks `ci` y `policy` correctos de la PR de configuración. Estos cambios administrativos se ejecutan solo con autorización del propietario; los JSON en `config/` permiten revisarlos antes de aplicar. Conserva la protección después del bootstrap.

Los hooks ayudan a evitar errores locales; las reglas remotas son las que protegen GitHub. El hook de pre-push rechaza enviar directamente a `main`. El de commit comprueba el mensaje. Cada nuevo clon debe ejecutar `install-hooks.ps1`.

Si `git pull --ff-only` rechaza una divergencia, revisa `git status` y `git log --oneline --graph --all`; no uses `reset --hard` para ocultarla. Para deshacer un cambio integrado, prepara una PR con `git revert` y mensaje conforme a la convención. Una reversión de Git no revierte pagos ni migraciones aplicadas. Ante una credencial expuesta, revócala antes de intentar limpiar el historial.

Referencias de operación: [GitHub Flow](https://docs.github.com/en/get-started/using-github/github-flow), [rulesets](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-rulesets/about-rulesets), [CODEOWNERS](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/about-code-owners) y la estrategia corregida local. Los documentos originales conservan sus identificadores y fechas.
