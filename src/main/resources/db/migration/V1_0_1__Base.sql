CREATE TABLE workspaces (
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    name VARCHAR NOT NULL,
    created_at TIMESTAMP DEFAULT current_timestamp,
    updated_at TIMESTAMP DEFAULT current_timestamp
);

CREATE TABLE grids (
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    name VARCHAR NOT NULL,
    workspace_id uuid NOT NULL,
    created_at TIMESTAMP DEFAULT current_timestamp,
    updated_at TIMESTAMP DEFAULT current_timestamp
);

ALTER TABLE grids ADD FOREIGN KEY (workspace_id) REFERENCES workspaces(id);

CREATE TABLE users (
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    username VARCHAR NOT NULL,
    email VARCHAR UNIQUE NOT NULL,
    hash VARCHAR NOT NULL,
    picture VARCHAR,
    role VARCHAR DEFAULT 'operator',
    current_workspace uuid,
    current_grid uuid,
    last_login TIMESTAMP DEFAULT current_timestamp,
    created_at TIMESTAMP DEFAULT current_timestamp,
    updated_at TIMESTAMP DEFAULT current_timestamp
);

ALTER TABLE users ADD FOREIGN KEY (current_workspace) REFERENCES workspaces(id);
ALTER TABLE users ADD FOREIGN KEY (current_grid) REFERENCES grids(id);

CREATE TABLE nodes (
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    name VARCHAR UNIQUE NOT NULL,
    active BOOLEAN DEFAULT true,
    grid_id uuid NOT NULL,
    location GEOMETRY(POINT) NOT NULL,
    created_by uuid NOT NULL,
    created_at TIMESTAMP DEFAULT current_timestamp,
    updated_by uuid NOT NULL,
    updated_at TIMESTAMP DEFAULT current_timestamp
);

ALTER TABLE nodes ADD FOREIGN KEY (grid_id) REFERENCES grids(id);
ALTER TABLE nodes ADD FOREIGN KEY (created_by) REFERENCES users(id);
ALTER TABLE nodes ADD FOREIGN KEY (updated_by) REFERENCES users(id);

CREATE TYPE device_type AS ENUM('meter', 'generator');

CREATE TABLE devices (
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    api_key VARCHAR NOT NULL,
    node_id uuid,
    type device_type DEFAULT 'meter',
    location GEOMETRY(POINT) NOT NULL,
    created_by uuid NOT NULL,
    created_at TIMESTAMP DEFAULT current_timestamp,
    updated_by uuid NOT NULL,
    updated_at TIMESTAMP DEFAULT current_timestamp
);

ALTER TABLE devices ADD FOREIGN KEY (node_id) REFERENCES nodes(id);
ALTER TABLE devices ADD FOREIGN KEY (created_by) REFERENCES users(id);
ALTER TABLE devices ADD FOREIGN KEY (updated_by) REFERENCES users(id);