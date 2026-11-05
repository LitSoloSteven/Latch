BEGIN;

CREATE SCHEMA IF NOT EXISTS latch;

CREATE TABLE latch.rol (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    nombre VARCHAR(16) NOT NULL,
    CONSTRAINT pk_rol PRIMARY KEY (id),
    CONSTRAINT uq_rol_nombre UNIQUE (nombre),
    CONSTRAINT ck_rol_nombre CHECK (nombre IN ('Propietario', 'Asistente'))
);

CREATE TABLE latch.usuario (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    rol_id UUID NOT NULL,
    nombre_usuario VARCHAR(40) NOT NULL,
    nombre_visible VARCHAR(120) NOT NULL,
    hash_clave TEXT NOT NULL,
    cambio_inicial BOOLEAN NOT NULL DEFAULT TRUE,
    intentos_fallidos INTEGER NOT NULL DEFAULT 0,
    bloqueado_hasta TIMESTAMPTZ,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creado_por UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT fk_usuario_rol_id FOREIGN KEY (rol_id) REFERENCES latch.rol (id) ON DELETE RESTRICT,
    CONSTRAINT fk_usuario_creado_por FOREIGN KEY (creado_por) REFERENCES latch.usuario (id) ON DELETE RESTRICT,
    CONSTRAINT ck_usuario_nombre_usuario CHECK (nombre_usuario ~ '^[A-Za-z0-9_.]{3,40}$'),
    CONSTRAINT ck_usuario_hash_clave CHECK (hash_clave LIKE '$argon2id$%'),
    CONSTRAINT ck_usuario_intentos_fallidos CHECK (intentos_fallidos >= 0),
    CONSTRAINT ck_usuario_version CHECK (version >= 0)
);

CREATE UNIQUE INDEX uq_usuario_nombre ON latch.usuario (lower(nombre_usuario));

INSERT INTO latch.rol (id, nombre) VALUES
    ('d4010000-0000-4000-8000-000000000001', 'Propietario'),
    ('d4010000-0000-4000-8000-000000000002', 'Asistente');

COMMIT;
