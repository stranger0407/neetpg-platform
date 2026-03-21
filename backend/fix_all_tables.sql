-- Fix all table names to match Hibernate Entity annotations
-- Step 1: Drop all foreign key constraints first
ALTER TABLE chapter DROP CONSTRAINT IF EXISTS chapter_subject_id_fkey;
ALTER TABLE question DROP CONSTRAINT IF EXISTS question_chapter_id_fkey;

-- Step 2: Rename tables to plural form
ALTER TABLE chapter RENAME TO chapters;
ALTER TABLE question RENAME TO questions;
ALTER TABLE "user" RENAME TO users;

-- Step 3: Recreate foreign key constraints with correct table names  
ALTER TABLE chapters ADD CONSTRAINT chapters_subject_id_fkey FOREIGN KEY (subject_id) REFERENCES subjects(id);
ALTER TABLE questions ADD CONSTRAINT questions_chapter_id_fkey FOREIGN KEY (chapter_id) REFERENCES chapters(id);

-- Step 4: Verify tables are renamed
SELECT 'TABLE: ' || tablename FROM pg_tables WHERE schemaname='public' ORDER BY tablename;
SELECT * FROM information_schema.table_constraints WHERE table_schema='public' ORDER BY table_name;
