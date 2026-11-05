INSERT INTO latch.propiedad (id, nombre, direccion, creado_por)
VALUES (
    'd4040000-0000-4000-8000-000000000001',
    'Propiedad de demostración',
    'Dirección sintética para unidades',
    'd4030000-0000-4000-8000-000000000001'
)
ON CONFLICT (id) DO NOTHING;
