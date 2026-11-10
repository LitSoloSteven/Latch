BEGIN;

CREATE TABLE latch.inquilino (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    nombre VARCHAR(160) NOT NULL,
    identificacion VARCHAR(40),
    telefono VARCHAR(30),
    correo VARCHAR(254),
    contacto_emergencia VARCHAR(200),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creado_por UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_inquilino PRIMARY KEY (id),
    CONSTRAINT ck_inquilino_version CHECK (version >= 0),
    CONSTRAINT fk_inquilino_creado_por FOREIGN KEY (creado_por) REFERENCES latch.usuario(id) ON DELETE RESTRICT
);

COMMIT;
