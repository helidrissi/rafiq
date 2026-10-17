CREATE TABLE patients (
    id UUID PRIMARY KEY,
    display_name VARCHAR(120) NOT NULL
);

CREATE TABLE slots (
    id UUID PRIMARY KEY,
    starts_at TIMESTAMPTZ NOT NULL,
    reserved BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE appointments (
    id UUID PRIMARY KEY,
    patient_id UUID NOT NULL REFERENCES patients(id),
    slot_id UUID NOT NULL UNIQUE REFERENCES slots(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE conversations (
    id UUID PRIMARY KEY,
    patient_id UUID REFERENCES patients(id),
    state VARCHAR(40) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE escalations (
    id UUID PRIMARY KEY,
    conversation_id UUID REFERENCES conversations(id),
    reason VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE processed_messages (
    message_sid VARCHAR(80) PRIMARY KEY,
    received_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
