BEGIN;

CREATE TABLE latch.propiedad (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    nombre VARCHAR(120) NOT NULL,
    direccion VARCHAR(300) NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creado_por UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_propiedad PRIMARY KEY (id),
    CONSTRAINT ck_propiedad_version CHECK (version >= 0),
    CONSTRAINT fk_propiedad_creado_por FOREIGN KEY (creado_por) REFERENCES latch.usuario(id) ON DELETE RESTRICT
);

COMMIT;
