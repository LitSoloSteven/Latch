# Registro de configuración de GitHub

Fecha de inicio: 26 de octubre de 2026, America/Managua. Solicitud: conectar y configurar Latch de acuerdo con la estrategia GitFlow existente y proporcionar una guía al propietario.

## Decisión técnica D44 de adopción inicial

Se conserva ADR-15: `main` como única rama permanente, ramas cortas y revisión ajena. La solicitud del propietario autoriza preparar y aplicar la configuración técnica; no registra una aprobación académica colectiva ni una aceptación D47 del producto. Las candidatas y hotfix siguen pendientes de enmienda y no se crean anticipadamente.

El propietario autorizó expresamente invitar con escritura a `CTardencilla`, `Arisgav` y `wetalff`, ajustar integración y seguridad, crear etiquetas, hitos y Project si los permisos lo permiten y proteger `main` y tags. Las invitaciones requieren aceptación de sus destinatarios.

## Estado inicial observado

- GitHub: `LitSoloSteven/Latch`, público, `main` predeterminada, commit inicial `309d1287181a216086af8bb972ea4e6ed1ba058e` con README y .gitignore.
- Carpeta local `latch`: repositorio sin commits, sin remoto y con código y documentos locales. Se conservó el commit remoto como base y todos los archivos de trabajo. El índice previo quedó respaldado en `.git/index-before-github-setup.backup`.
- Identidad y acceso HTTPS comprobados como `LitSoloSteven`. Git Credential Manager disponible; no se exportaron tokens a archivos.
- Único colaborador efectivo observado al inicio: propietario. Sin rulesets iniciales.
- Git observado: 2.55.0.windows.4. JDK local observado: Temurin 21.0.12+8. No se cambió configuración global ni se generaron claves privadas.

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

El Wrapper existente sigue usando Maven 3.8.5 y el POM Compiler 3.13.0. El TRD propone Maven 3.9.16, Compiler 3.14.0 y Surefire 3.5.3, además de la arquitectura y dependencias completas. La actualización canónica, pruebas unitarias e integración con BD, cobertura con ausencia de clases como fallo y empaquetado Windows siguen pendientes de TAR-004 y las tareas correspondientes. No se sustituyó el POM ni se inventaron pruebas para convertir el arranque en aceptación de producto.

No hay workflow de publicación automática de Releases ni automatización de Projects con tokens. Esos controles requieren runtime JavaFX verificado, migraciones y pruebas reales, aprobación de la enmienda cuando corresponda y permisos de cuenta. La firma personal SSH/GPG y la autenticación de dos factores corresponden a cada integrante; no se exige una firma todavía sin configurar.

Los workflows de PR ejecutan código con permisos de lectura y sin secretos de producto. Los cambios de scripts y workflows requieren revisión humana. Las versiones de Actions están fijadas por etiqueta exacta; la migración a SHA verificados queda como mejora de endurecimiento.

## Resultado remoto

Pendiente de completar con consultas a GitHub al finalizar la aplicación. Los archivos preparados no acreditan por sí mismos ajustes remotos activos. La PR debe revisarse desde otra cuenta antes de integrarse; no se autoaprueba.
