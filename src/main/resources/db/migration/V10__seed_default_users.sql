INSERT INTO users (id, name, email, password_hash, profile_id, created_at, updated_at)
VALUES
    (
        uuid_generate_v4(),
        'Administrador Padrão',
        'admin@mechanicalhub.com',
        '$2a$10$wRWktYa3plYYEYcbQr8leOBrNmloJE0s1awb7yuHedmjiUjbAvIH6',
        (SELECT id FROM profiles WHERE name = 'ADMINISTRATOR'),
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        uuid_generate_v4(),
        'Mecânico Padrão',
        'mecanico@mechanicalhub.com',
        '$2a$10$wRWktYa3plYYEYcbQr8leOBrNmloJE0s1awb7yuHedmjiUjbAvIH6',
        (SELECT id FROM profiles WHERE name = 'MECHANICAL'),
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
    ON CONFLICT DO NOTHING;
