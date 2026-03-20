-- Seed some test data
INSERT INTO subject (name, created_at, updated_at) VALUES
('Anatomy', NOW(), NOW()),
('Physiology', NOW(), NOW()),
('Biochemistry', NOW(), NOW()),
('Pathology', NOW(), NOW()),
('Pharmacology', NOW(), NOW()),
('Microbiology', NOW(), NOW()),
('Forensic Medicine', NOW(), NOW()),
('Community Medicine', NOW(), NOW()),
('ENT', NOW(), NOW()),
('Ophthalmology', NOW(), NOW()),
('Medicine', NOW(), NOW()),
('Surgery', NOW(), NOW()),
('Obstetrics and Gynecology', NOW(), NOW()),
('Pediatrics', NOW(), NOW()),
('Orthopedics', NOW(), NOW()),
('Dermatology', NOW(), NOW()),
('Psychiatry', NOW(), NOW()),
('Radiology', NOW(), NOW()),
('Anesthesia', NOW(), NOW());

-- Create admin user
INSERT INTO "user" (name, email, password, role, created_at, updated_at)
VALUES ('Admin', 'admin@neetpg.com', '$2a$10$YourHashedPasswordHere', 'ADMIN', NOW(), NOW());

-- Create demo student
INSERT INTO "user" (name, email, password, role, created_at, updated_at)
VALUES ('Demo Student', 'student@neetpg.com', '$2a$10$YourHashedPasswordHere', 'STUDENT', NOW(), NOW());
