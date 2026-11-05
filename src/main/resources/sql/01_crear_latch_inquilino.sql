-- ==============================================================================
-- Latch - Esquema y tabla de persistencia para Inquilino (#5)
-- Tabla asignada: latch.inquilino
-- Conforme a docs/Latch_Backend_Schema_APA7_v3.docx, Apendice A y Tabla D5
-- ==============================================================================

CREATE SCHEMA IF NOT EXISTS latch;

CREATE TABLE IF NOT EXISTS latch.inquilino (
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
    CONSTRAINT ck_inquilino_version CHECK (version >= 0)
);

-- Clave foranea de auditoria hacia latch.usuario (si la tabla existe en el entorno)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_schema = 'latch' AND table_name = 'usuario'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_inquilino_creado_por' AND table_schema = 'latch'
    ) THEN
        ALTER TABLE latch.inquilino 
        ADD CONSTRAINT fk_inquilino_creado_por 
        FOREIGN KEY (creado_por) REFERENCES latch.usuario(id) ON DELETE RESTRICT;
    END IF;
END $$;
