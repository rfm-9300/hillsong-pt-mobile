-- V4__Add_password_reset_columns.sql

-- Add reset token columns to the user table
ALTER TABLE public."user"
ADD COLUMN reset_token VARCHAR(255) NULL,
ADD COLUMN reset_token_expires_at BIGINT NULL;

-- Create a table to track password reset tokens
CREATE TABLE public.password_reset (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    token VARCHAR(255) NOT NULL,
    expires_at BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES public."user" (id) ON DELETE CASCADE
);

-- Add indexes for faster lookups
CREATE INDEX idx_password_reset_token ON public.password_reset(token);
CREATE INDEX idx_password_reset_user_id ON public.password_reset(user_id);
CREATE INDEX idx_user_reset_token ON public."user"(reset_token); 