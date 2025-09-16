-- V7__Update_attendance_table_structure.sql
-- Update attendance table to match test data structure and support both user and kid attendance

-- Drop the existing attendance table if it exists (since we need to restructure it)
DROP TABLE IF EXISTS public.attendance CASCADE;

-- Recreate attendance table with the correct structure to match test data
CREATE TABLE public.attendance (
    id SERIAL PRIMARY KEY,
    event_type VARCHAR(20) NOT NULL,
    event_id INTEGER NOT NULL,
    user_id INTEGER,
    kid_id INTEGER,
    checked_in_by INTEGER NOT NULL,
    check_in_time TIMESTAMP NOT NULL DEFAULT NOW(),
    check_out_time TIMESTAMP,
    checked_out_by INTEGER,
    status VARCHAR(20) NOT NULL DEFAULT 'CHECKED_IN',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_attendance_user FOREIGN KEY (user_id) REFERENCES public."users" (id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_kid FOREIGN KEY (kid_id) REFERENCES public.kid (id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_checked_in_by FOREIGN KEY (checked_in_by) REFERENCES public."users" (id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_checked_out_by FOREIGN KEY (checked_out_by) REFERENCES public."users" (id) ON DELETE CASCADE,
    CONSTRAINT chk_attendance_event_type CHECK (event_type IN ('EVENT', 'SERVICE', 'KIDS_SERVICE')),
    CONSTRAINT chk_attendance_status CHECK (status IN ('CHECKED_IN', 'CHECKED_OUT', 'NO_SHOW', 'CANCELLED')),
    CONSTRAINT chk_attendance_user_or_kid CHECK (
        (user_id IS NOT NULL AND kid_id IS NULL) OR
        (user_id IS NULL AND kid_id IS NOT NULL)
    )
);

-- Add foreign key constraints based on event_type and event_id
-- Note: PostgreSQL doesn't support conditional foreign keys directly, 
-- so we'll handle this in the application layer

-- Create indexes for the attendance table
CREATE INDEX idx_attendance_event_type ON public.attendance(event_type);
CREATE INDEX idx_attendance_event_id ON public.attendance(event_id);
CREATE INDEX idx_attendance_user_id ON public.attendance(user_id);
CREATE INDEX idx_attendance_kid_id ON public.attendance(kid_id);
CREATE INDEX idx_attendance_checked_in_by ON public.attendance(checked_in_by);
CREATE INDEX idx_attendance_check_in_time ON public.attendance(check_in_time);
CREATE INDEX idx_attendance_status ON public.attendance(status);

-- Update the service table to match test data structure
-- The service table in test data has different column names
ALTER TABLE public.service 
DROP COLUMN IF EXISTS day_of_week,
DROP COLUMN IF EXISTS start_time,
DROP COLUMN IF EXISTS end_time,
ADD COLUMN IF NOT EXISTS start_time TIMESTAMP,
ADD COLUMN IF NOT EXISTS end_time TIMESTAMP;

-- Update service table constraints
ALTER TABLE public.service 
DROP CONSTRAINT IF EXISTS chk_day_of_week,
DROP CONSTRAINT IF EXISTS chk_service_type;

-- Add updated constraints for service_type
ALTER TABLE public.service 
ADD CONSTRAINT chk_service_type CHECK (service_type IN ('REGULAR', 'YOUTH', 'PRAYER', 'SPECIAL', 'SUNDAY_WORSHIP', 'BIBLE_STUDY', 'PRAYER_MEETING', 'YOUTH_SERVICE', 'SMALL_GROUP', 'SPECIAL_EVENT', 'COMMUNITY_OUTREACH', 'DISCIPLESHIP', 'WORSHIP_PRACTICE', 'LEADERSHIP_MEETING', 'OTHER'));

-- Update kids_service table to match the expected structure
ALTER TABLE public.kids_service 
DROP COLUMN IF EXISTS day_of_week,
DROP COLUMN IF EXISTS start_time,
DROP COLUMN IF EXISTS end_time,
ADD COLUMN IF NOT EXISTS service_id INTEGER,
ADD CONSTRAINT fk_kids_service_service FOREIGN KEY (service_id) REFERENCES public.service (id) ON DELETE CASCADE;

-- Update kids_service table to have age_group_min and age_group_max columns to match test data
ALTER TABLE public.kids_service 
ADD COLUMN IF NOT EXISTS age_group_min INTEGER,
ADD COLUMN IF NOT EXISTS age_group_max INTEGER;

-- Update constraints for kids_service
ALTER TABLE public.kids_service 
DROP CONSTRAINT IF EXISTS chk_kids_service_day_of_week;