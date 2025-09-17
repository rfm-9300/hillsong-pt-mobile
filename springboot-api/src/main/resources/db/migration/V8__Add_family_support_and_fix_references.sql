-- V8__Add_family_support_and_fix_references.sql
-- Add family_id support and fix table references to match test data

-- Add family_id to kid table to match test data structure
ALTER TABLE public.kid 
ADD COLUMN IF NOT EXISTS family_id INTEGER;

-- Add foreign key constraint for family_id (references user_profile id)
ALTER TABLE public.kid 
ADD CONSTRAINT fk_kid_family FOREIGN KEY (family_id) REFERENCES public.user_profile (id) ON DELETE CASCADE;

-- Create index for family_id
CREATE INDEX IF NOT EXISTS idx_kid_family_id ON public.kid(family_id);

-- Update the password_reset table to ensure it matches the existing structure
-- The table already exists from V4, but let's ensure it has the correct structure
ALTER TABLE public.password_reset 
ALTER COLUMN user_id TYPE INTEGER;

-- Add foreign key constraint (drop first if exists)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_password_reset_user_v2') THEN
        ALTER TABLE public.password_reset 
        ADD CONSTRAINT fk_password_reset_user_v2 FOREIGN KEY (user_id) REFERENCES public."users" (id) ON DELETE CASCADE;
    END IF;
END $$;

-- Ensure the password_reset table has the correct indexes
CREATE INDEX IF NOT EXISTS idx_password_reset_token_v2 ON public.password_reset(token);
CREATE INDEX IF NOT EXISTS idx_password_reset_user_id_v2 ON public.password_reset(user_id);

-- Add any missing columns to users table that might be needed
ALTER TABLE public."users"
ADD COLUMN IF NOT EXISTS username VARCHAR(128);

-- Create a unique index on username if it doesn't exist
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_username ON public."users"(username) WHERE username IS NOT NULL;

-- Update the user_profile table to ensure email matches user email
-- This will be handled by the application, but we can add a trigger or constraint if needed

-- Ensure all tables have proper created_at and updated_at columns where needed
ALTER TABLE public.event 
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

ALTER TABLE public.post 
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW(),
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

ALTER TABLE public.user_profile 
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW(),
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- Add any missing indexes for performance
CREATE INDEX IF NOT EXISTS idx_event_created_at ON public.event(created_at);
CREATE INDEX IF NOT EXISTS idx_post_created_at ON public.post(created_at);
CREATE INDEX IF NOT EXISTS idx_user_profile_created_at ON public.user_profile(created_at);

-- Ensure proper constraints on timestamp columns
ALTER TABLE public.event 
ALTER COLUMN created_at SET DEFAULT NOW();

ALTER TABLE public.post 
ALTER COLUMN created_at SET DEFAULT NOW();

ALTER TABLE public.user_profile 
ALTER COLUMN created_at SET DEFAULT NOW();