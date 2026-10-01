ALTER TABLE prestador ADD COLUMN email VARCHAR(255);
ALTER TABLE prestador ADD COLUMN senha_hash VARCHAR(255);

UPDATE prestador SET email = 'prestador-' || id || '@placeholder.local' WHERE email IS NULL;
UPDATE prestador SET senha_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' WHERE senha_hash IS NULL;

ALTER TABLE prestador ALTER COLUMN email SET NOT NULL;
ALTER TABLE prestador ALTER COLUMN senha_hash SET NOT NULL;
ALTER TABLE prestador ADD CONSTRAINT uk_prestador_email UNIQUE (email);
