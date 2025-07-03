-- V3__Add_id_column_to_user_table.sql

-- Drop the existing primary key constraint if it exists
ALTER TABLE public."user"
DROP CONSTRAINT IF EXISTS user_pkey;

-- Add a new 'id' column as the primary key
ALTER TABLE public."user"
ADD COLUMN id SERIAL PRIMARY KEY;

-- Add a unique constraint on the 'username' column to ensure it remains unique
ALTER TABLE public."user"
ADD CONSTRAINT user_username_unique UNIQUE (username);
