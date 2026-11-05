-- ==============================================================================
-- Latch - Datos sinteticos para Inquilino (#5)
-- Preparar al menos tres inquilinos sinteticos, incluyendo uno sin telefono ni correo
-- ==============================================================================

-- Inquilino 1: Registro completo
INSERT INTO latch.inquilino (
    id, nombre, identificacion, telefono, correo, contacto_emergencia, activo, creado_en, creado_por, version
) VALUES (
    'a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d',
    'Carlos Alberto Mendoza Ruiz',
    '001-150392-0004X',
    '+505 8899-7711',
    'carlos.mendoza@email.com',
    'Elena Ruiz (Madre) - +505 8811-2233',
    TRUE,
    CURRENT_TIMESTAMP,
    NULL,
    0
) ON CONFLICT (id) DO NOTHING;

-- Inquilino 2: Registro completo alternativo
INSERT INTO latch.inquilino (
    id, nombre, identificacion, telefono, correo, contacto_emergencia, activo, creado_en, creado_por, version
) VALUES (
    'b2c3d4e5-f6a7-4b5c-9d0e-1f2a3b4c5d6e',
    'Lucia Sofia Morales Gomez',
    '281-220795-0002Y',
    '+505 7744-1122',
    'lucia.morales@empresa.com',
    'Roberto Morales (Hermano) - +505 8833-4455',
    TRUE,
    CURRENT_TIMESTAMP,
    NULL,
    0
) ON CONFLICT (id) DO NOTHING;

-- Inquilino 3: Registro sin telefono ni correo (campos opcionales en NULL)
INSERT INTO latch.inquilino (
    id, nombre, identificacion, telefono, correo, contacto_emergencia, activo, creado_en, creado_por, version
) VALUES (
    'c3d4e5f6-a7b8-4c5d-0e1f-2a3b4c5d6e7f',
    'Marvin Jose Castillo Blandon',
    '441-051188-0001Z',
    NULL,
    NULL,
    NULL,
    TRUE,
    CURRENT_TIMESTAMP,
    NULL,
    0
) ON CONFLICT (id) DO NOTHING;
