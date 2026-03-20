CREATE TABLE IF NOT EXISTS "user" (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    role VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS subject (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS chapter (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    subject_id BIGINT REFERENCES subject(id),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS question (
    id BIGSERIAL PRIMARY KEY,
    chapter_id BIGINT REFERENCES chapter(id),
    question_text TEXT,
    option_a TEXT,
    option_b TEXT,
    option_c TEXT,
    option_d TEXT,
    correct_answer VARCHAR(1),
    explanation TEXT,
    difficulty VARCHAR(50),
    source VARCHAR(255),
    tags VARCHAR(500),
    previous_year BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
