-- The file is a quick workaround to properly initialize the PostgreSQL in Testcontainers
-- The file shall be identical to the docker/db/init-db.sql!
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE SCHEMA IF NOT EXISTS core;