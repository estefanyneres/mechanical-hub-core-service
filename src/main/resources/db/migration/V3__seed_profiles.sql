INSERT INTO profiles (name, description) VALUES ('MECHANICAL', 'Mecânico')
ON CONFLICT (name) DO NOTHING;

INSERT INTO profiles (name, description) VALUES ('ADMINISTRATOR', 'Administrador')
ON CONFLICT (name) DO NOTHING;
