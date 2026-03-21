-- Fix all table names to match Hibernate entity expectations
ALTER TABLE chapter RENAME TO chapters;
ALTER TABLE question RENAME TO questions;
ALTER TABLE "user" RENAME TO users;

-- Verify renamed tables
SELECT table_name FROM information_schema.tables WHERE table_schema='public' ORDER BY table_name;
