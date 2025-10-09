-- V15__Create_roles_and_user_roles_tables.sql
-- Create roles table and user_roles junction table for proper role management

-- Create roles table
CREATE TABLE IF NOT EXISTS public.role (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Create user_roles junction table for many-to-many relationship
CREATE TABLE IF NOT EXISTS public.user_roles (
    user_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,
    granted_at TIMESTAMP NOT NULL DEFAULT NOW(),
    granted_by INTEGER,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES public."users" (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES public.role (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_granted_by FOREIGN KEY (granted_by) REFERENCES public."users" (id) ON DELETE SET NULL
);

-- Insert default roles
INSERT INTO public.role (name, description) VALUES
    ('USER', 'Standard user with basic permissions'),
    ('ADMIN', 'Administrator with full system access'),
    ('STAFF', 'Kids ministry staff member with check-in verification permissions')
ON CONFLICT (name) DO NOTHING;

-- Migrate existing admin users to new role system
-- First, grant USER role to all existing users
INSERT INTO public.user_roles (user_id, role_id)
SELECT u.id, r.id
FROM public."users" u
CROSS JOIN public.role r
WHERE r.name = 'USER'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- Grant ADMIN role to users who have is_admin = true
INSERT INTO public.user_roles (user_id, role_id)
SELECT u.id, r.id
FROM public."users" u
INNER JOIN public.user_profile up ON u.id = up.user_id
CROSS JOIN public.role r
WHERE up.is_admin = true AND r.name = 'ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON public.user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON public.user_roles(role_id);
CREATE INDEX IF NOT EXISTS idx_role_name ON public.role(name);

-- Note: We're keeping the is_admin column in user_profile for backward compatibility
-- It will be deprecated in a future migration
