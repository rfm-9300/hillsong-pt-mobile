-- V5__Create_complete_schema_for_spring_boot.sql
-- Complete schema migration for Spring Boot JPA entities

-- First, rename the existing user table to users and update structure
ALTER TABLE public."user" RENAME TO "users";

-- Update users table structure to match JPA entity
ALTER TABLE public."users"
RENAME COLUMN username TO email;

-- Add missing columns to users table
ALTER TABLE public."users"
ADD COLUMN IF NOT EXISTS verified BOOLEAN NOT NULL DEFAULT false,
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT NOW(),
ADD COLUMN IF NOT EXISTS verification_token VARCHAR(256),
ADD COLUMN IF NOT EXISTS google_id VARCHAR(256),
ADD COLUMN IF NOT EXISTS facebook_id VARCHAR(256),
ADD COLUMN IF NOT EXISTS auth_provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL';

-- Update constraints
ALTER TABLE public."users"
DROP CONSTRAINT IF EXISTS user_username_unique,
ADD CONSTRAINT users_email_unique UNIQUE (email);

-- Create user_profile table
CREATE TABLE IF NOT EXISTS public.user_profile (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL UNIQUE,
    first_name VARCHAR(128) NOT NULL,
    last_name VARCHAR(128) NOT NULL,
    email VARCHAR(128) NOT NULL,
    phone VARCHAR(18) NOT NULL DEFAULT '',
    joined_at TIMESTAMP NOT NULL DEFAULT NOW(),
    image_path VARCHAR(500) NOT NULL DEFAULT '',
    is_admin BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT fk_user_profile_user FOREIGN KEY (user_id) REFERENCES public."users" (id) ON DELETE CASCADE
);

-- Create event table
CREATE TABLE IF NOT EXISTS public.event (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    date TIMESTAMP NOT NULL,
    location VARCHAR(255) NOT NULL,
    organizer_id INTEGER NOT NULL,
    header_image_path VARCHAR(255) NOT NULL DEFAULT '',
    max_attendees INTEGER NOT NULL,
    needs_approval BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_event_organizer FOREIGN KEY (organizer_id) REFERENCES public.user_profile (id) ON DELETE CASCADE
);

-- Create event_attendee junction table
CREATE TABLE IF NOT EXISTS public.event_attendee (
    event_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    PRIMARY KEY (event_id, user_id),
    CONSTRAINT fk_event_attendee_event FOREIGN KEY (event_id) REFERENCES public.event (id) ON DELETE CASCADE,
    CONSTRAINT fk_event_attendee_user FOREIGN KEY (user_id) REFERENCES public."users" (id) ON DELETE CASCADE
);

-- Create event_waiting_list junction table
CREATE TABLE IF NOT EXISTS public.event_waiting_list (
    event_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    PRIMARY KEY (event_id, user_id),
    CONSTRAINT fk_event_waiting_list_event FOREIGN KEY (event_id) REFERENCES public.event (id) ON DELETE CASCADE,
    CONSTRAINT fk_event_waiting_list_user FOREIGN KEY (user_id) REFERENCES public."users" (id) ON DELETE CASCADE
);

-- Create post table
CREATE TABLE IF NOT EXISTS public.post (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    date TIMESTAMP NOT NULL DEFAULT NOW(),
    header_image_path VARCHAR(255) NOT NULL DEFAULT 'default-header.jpg',
    user_id INTEGER NOT NULL,
    CONSTRAINT fk_post_user FOREIGN KEY (user_id) REFERENCES public."users" (id) ON DELETE CASCADE
);

-- Create post_like junction table
CREATE TABLE IF NOT EXISTS public.post_like (
    post_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    PRIMARY KEY (post_id, user_id),
    CONSTRAINT fk_post_like_post FOREIGN KEY (post_id) REFERENCES public.post (id) ON DELETE CASCADE,
    CONSTRAINT fk_post_like_user FOREIGN KEY (user_id) REFERENCES public."users" (id) ON DELETE CASCADE
);

-- Create post_comment table
CREATE TABLE IF NOT EXISTS public.post_comment (
    id SERIAL PRIMARY KEY,
    post_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    content TEXT NOT NULL,
    date TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_post_comment_post FOREIGN KEY (post_id) REFERENCES public.post (id) ON DELETE CASCADE,
    CONSTRAINT fk_post_comment_user FOREIGN KEY (user_id) REFERENCES public."users" (id) ON DELETE CASCADE
);

-- Create user_token table
CREATE TABLE IF NOT EXISTS public.user_token (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    access_token VARCHAR(512) NOT NULL,
    refresh_token VARCHAR(512) NOT NULL,
    access_token_expires_at BIGINT NOT NULL,
    refresh_token_expires_at BIGINT NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT false,
    device_info VARCHAR(256),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_used_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_user_token_user FOREIGN KEY (user_id) REFERENCES public."users" (id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_email ON public."users"(email);
CREATE INDEX IF NOT EXISTS idx_users_verification_token ON public."users"(verification_token);
CREATE INDEX IF NOT EXISTS idx_users_reset_token ON public."users"(reset_token);
CREATE INDEX IF NOT EXISTS idx_user_profile_user_id ON public.user_profile(user_id);
CREATE INDEX IF NOT EXISTS idx_event_date ON public.event(date);
CREATE INDEX IF NOT EXISTS idx_event_organizer_id ON public.event(organizer_id);
CREATE INDEX IF NOT EXISTS idx_post_user_id ON public.post(user_id);
CREATE INDEX IF NOT EXISTS idx_post_date ON public.post(date);
CREATE INDEX IF NOT EXISTS idx_user_token_user_id ON public.user_token(user_id);
CREATE INDEX IF NOT EXISTS idx_user_token_access_token ON public.user_token(access_token);