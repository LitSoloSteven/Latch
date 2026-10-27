# Registro de configuración de GitHub

Fecha de inicio: 26 de octubre de 2026, America/Managua. Alcance: conexión del repositorio local con GitHub y configuración del trabajo del equipo según la estrategia GitFlow de Latch.

## Decisión técnica D44 de adopción inicial

La configuración sigue ADR-15: `main` como única rama permanente, ramas cortas y revisión ajena. Su adopción técnica se registra bajo D44; la revisión académica y la aceptación D47 del producto se registran por separado. Las candidatas y hotfix siguen pendientes de enmienda.

Steven autorizó las invitaciones con escritura a `CTardencilla`, `Arisgav` y `wetalff`, los ajustes de integración y seguridad, las etiquetas, los hitos, el Project sujeto a permisos y la protección de `main` y tags. Cada integrante acepta la invitación desde su cuenta.

## Estado inicial observado

- GitHub: `LitSoloSteven/Latch`, público, `main` predeterminada, commit inicial `309d1287181a216086af8bb972ea4e6ed1ba058e` con README y .gitignore.
- Carpeta local `latch`: repositorio sin commits, sin remoto y con código y documentos locales. Se conservó el commit remoto como base y todos los archivos de trabajo. El índice previo quedó respaldado en `.git/index-before-github-setup.backup`.
- Identidad y acceso HTTPS comprobados como `LitSoloSteven`. Git Credential Manager disponible; no se exportaron tokens a archivos.
- Único colaborador efectivo observado al inicio: propietario. Sin rulesets iniciales.
- Entorno local: Git 2.55.0.windows.4 y Temurin 21.0.12+8. Los ajustes se limitaron al repositorio; la configuración global y las claves privadas permanecieron sin cambios.

## Archivos y configuración preparada

- Remoto `origin`, seguimiento local de `main` y rama `build/TAR-004-configurar-repositorio`.
- Opciones Git locales para LF, pull fast-forward y limpieza de referencias; hooks de mensaje y rechazo de push directo a `main`.
- `.gitignore`, `.gitattributes`, `.editorconfig`, CODEOWNERS con las cuentas confirmadas y siete formularios de Issues.
- Plantilla de PR con evidencia y mensaje real de squash; validador compartido de commits y PR.
- Workflows `ci` y `policy`, permisos de solo lectura y credenciales de checkout no persistentes; Dependabot semanal para Maven y Actions.
- 47 etiquetas, 16 hitos y tres rulesets en JSON, junto con script administrativo reproducible.
- Bloqueo propuesto de eliminación y force push de `main`, PR con una aprobación ajena, revisión de Code Owner, descarte de aprobaciones obsoletas, aprobación del último push ajena, conversaciones resueltas, historial lineal, squash y checks estrictos `ci`/`policy` de GitHub Actions.
- Dos reglas separadas de tags `v*`: solo administración crea y nadie modifica o elimina tags publicados. No se crea ningún tag de versión sin aceptación.

## Validación y límites

`mvnw.cmd -B -ntp verify` completó BUILD SUCCESS con Java 21 y Maven 3.8.5. El esqueleto no contiene `src/test`; Maven informó `No tests to run`. Por tanto, el check de arranque `ci` acredita construcción del esqueleto, no pruebas de producto, cobertura del 80 %, PostgreSQL, cien CP ni funcionamiento visual de la aplicación.

El Wrapper existente sigue usando Maven 3.8.5 y el POM Compiler 3.13.0. El TRD propone Maven 3.9.16, Compiler 3.14.0 y Surefire 3.5.3, además de la arquitectura y dependencias completas. La actualización canónica, pruebas unitarias e integración con BD, cobertura con ausencia de clases como fallo y empaquetado Windows siguen pendientes de TAR-004 y las tareas correspondientes. El POM y la ausencia de pruebas de producto se conservan como estado de partida.

No hay workflow de publicación automática de Releases ni automatización de Projects con tokens. Esos controles requieren runtime JavaFX verificado, migraciones y pruebas reales, aprobación de la enmienda cuando corresponda y permisos de cuenta. La firma personal SSH/GPG y la autenticación de dos factores corresponden a cada integrante; no se exige una firma todavía sin configurar.

Los workflows de PR ejecutan código con permisos de lectura y sin secretos de producto. Los cambios de scripts y workflows requieren revisión de otro integrante. Las versiones de Actions están fijadas por etiqueta exacta; la migración a SHA verificados queda como mejora de endurecimiento.

## Resultado remoto

Resultados comprobados mediante Git y API el 26 de octubre de 2026, America/Managua:

- `origin` y seguimiento local conectados. La rama de configuración se publicó y el historial inicial remoto se conserva.
- [PR #2](https://github.com/LitSoloSteven/Latch/pull/2) abierta hacia `main`; [issue #1 / TAR-004](https://github.com/LitSoloSteven/Latch/issues/1) abierto, sin declarar aceptación ni horas reales. Se asignó seguimiento a Steven; Aris conserva la responsabilidad nominal de TAR-004 en el Plan.
- Squash y merge habilitados en General; rebase merge y auto-merge deshabilitados; eliminación de rama integrada habilitada. El ruleset de `main` limita su integración a squash.
- Secret scanning y push protection habilitados; alertas de vulnerabilidades habilitadas; permisos predeterminados de workflows en lectura, sin permitir que Actions apruebe PR.
- Las 47 etiquetas del proyecto se agregaron conservando las 10 preexistentes: 57 en total. Los 16 milestones se crearon con sus fechas oficiales.
- Invitaciones con escritura enviadas a `CTardencilla`, `Arisgav` y `wetalff`; pendientes de aceptación en la fecha del registro. Las solicitudes de revisión quedan pendientes hasta que tengan acceso efectivo al repositorio.
- Ruleset [main protegida, 25935179](https://github.com/LitSoloSteven/Latch/rules/25935179) activo, sin bypass: PR, una aprobación ajena y del último push, Code Owner, conversaciones resueltas, descarte de aprobaciones obsoletas, historial lineal, squash, prohibición de borrado/force push y checks estrictos `ci`/`policy` vinculados a GitHub Actions (app 15368).
- Rulesets [versiones inmutables, 25935181](https://github.com/LitSoloSteven/Latch/rules/25935181) y [creación de versiones, 25935183](https://github.com/LitSoloSteven/Latch/rules/25935183) activos para `v*`; la excepción del propietario para crear no permite actualizar o eliminar versiones.
- Primera ejecución real sobre `b58d06034d688fe1c32f61d060208ebafb34ee07`: [CI 40454658427](https://github.com/LitSoloSteven/Latch/actions/runs/40454658427) y [Policy 40454658435](https://github.com/LitSoloSteven/Latch/actions/runs/40454658435), ambas satisfactorias. Las reglas se activaron después de verificar esos nombres, resultados y proveedor. Los cambios posteriores de la PR deben repetir ambos checks antes de integrar.
- Sintaxis PowerShell y YAML/JSON válidas; ocho casos de mensajes comprobados; evento real de PR aceptado localmente y ramas/títulos inválidos rechazados; `git diff --check` correcto.
- Project pendiente de creación: la credencial dispone de los alcances `repo`, `workflow` y `gist`, pero la API de Projects requiere `project`. La guía incluye el procedimiento de creación desde la cuenta de Steven, con sus campos y vistas.

La PR debe revisarse desde otra cuenta y sigue abierta. CODEOWNERS, formularios, hooks compartidos, workflows y Dependabot se incorporarán a la rama predeterminada al integrar. Las protecciones, ajustes, etiquetas, hitos e invitaciones remotos ya están aplicados. El Project, la aceptación de invitaciones, las firmas personales y la aprobación ajena continúan pendientes de sus titulares.
