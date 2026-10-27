# Latch

Aplicación de escritorio JavaFX. Proyecto académico de Programación de Aplicaciones de Escritorio, UAM.

El código actual es el esqueleto inicial. Las especificaciones y el plan están en [docs](docs/).

## Empezar

Requisitos: Git, JDK 21 y acceso al repositorio. Maven se ejecuta con el Wrapper incluido.

```powershell
git clone https://github.com/LitSoloSteven/Latch.git
cd Latch
./scripts/install-hooks.ps1
./mvnw.cmd -B -ntp verify
./mvnw.cmd javafx:run
```

Configura `JAVA_HOME` con la ruta de tu JDK 21 si no está disponible en la terminal. IntelliJ debe usar también JDK 21 para el proyecto y Maven.

## Trabajo del equipo

`main` es la única rama permanente. Los cambios se proponen en ramas cortas mediante PR, con revisión de otra persona y squash. ADR-15 descarta `develop` permanente.

- [Guía práctica para Steven y el equipo](docs/GUIA_GITHUB.md)
- [Registro de configuración y límites de la CI](docs/CONFIGURACION_GITHUB.md)
- [Estrategia GitFlow corregida](docs/Latch_Estrategia_Gitflow_APA7_corregida.pdf)

La CI de arranque ejecuta `verify` sobre el esqueleto. Todavía no acredita pruebas de producto, cobertura, integración con PostgreSQL ni distribución ejecutable.
