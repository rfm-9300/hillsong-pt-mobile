-- V9__Verify_schema_and_add_constraints.sql
-- Final verification and additional constraints for data integrity

-- Add additional constraints and indexes for better data integrity and performance

-- Ensure email consistency between users and user_profile
-- Note: This will be enforced at the application level, but we can add a trigger if needed

-- Add check constraints for better data validation
ALTER TABLE public.event 
ADD CONSTRAINT chk_event_max_attendees CHECK (max_attendees > 0),
ADD CONSTRAINT chk_event_date_future CHECK (date > created_at);

ALTER TABLE public.post 
ADD CONSTRAINT chk_post_title_length CHECK (LENGTH(title) > 0),
ADD CONSTRAINT chk_post_content_length CHECK (LENGTH(content) > 0);

ALTER TABLE public.user_profile 
ADD CONSTRAINT chk_user_profile_name_length CHECK (LENGTH(first_name) > 0 AND LENGTH(last_name) > 0),
ADD CONSTRAINT chk_user_profile_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$');

ALTER TABLE public."users"
ADD CONSTRAINT chk_users_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
ADD CONSTRAINT chk_users_password_length CHECK (LENGTH(password) >= 8);

-- Add constraints for kids table
ALTER TABLE public.kid 
ADD CONSTRAINT chk_kid_name_length CHECK (LENGTH(first_name) > 0 AND LENGTH(last_name) > 0),
ADD CONSTRAINT chk_kid_birth_date CHECK (date_of_birth <= CURRENT_DATE);

-- Add constraints for service tables
ALTER TABLE public.service 
ADD CONSTRAINT chk_service_name_length CHECK (LENGTH(name) > 0),
ADD CONSTRAINT chk_service_location_length CHECK (LENGTH(location) > 0);

ALTER TABLE public.kids_service 
ADD CONSTRAINT chk_kids_service_name_length CHECK (LENGTH(name) > 0),
ADD CONSTRAINT chk_kids_service_location_length CHECK (LENGTH(location) > 0);

-- Add composite indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_attendance_event_type_event_id ON public.attendance(event_type, event_id);
CREATE INDEX IF NOT EXISTS idx_attendance_user_check_in_time ON public.attendance(user_id, check_in_time);
CREATE INDEX IF NOT EXISTS idx_attendance_kid_check_in_time ON public.attendance(kid_id, check_in_time);

CREATE INDEX IF NOT EXISTS idx_event_date_organizer ON public.event(date, organizer_id);
CREATE INDEX IF NOT EXISTS idx_post_date_user ON public.post(date, user_id);

CREATE INDEX IF NOT EXISTS idx_kid_family_active ON public.kid(family_id, is_active);
CREATE INDEX IF NOT EXISTS idx_service_active_type ON public.service(is_active, service_type);
CREATE INDEX IF NOT EXISTS idx_kids_service_active_age ON public.kids_service(is_active, min_age, max_age);

-- Add partial indexes for better performance on filtered queries
CREATE INDEX IF NOT EXISTS idx_users_verified ON public."users"(id) WHERE verified = true;
CREATE INDEX IF NOT EXISTS idx_users_unverified ON public."users"(id) WHERE verified = false;
CREATE INDEX IF NOT EXISTS idx_attendance_checked_in ON public.attendance(id) WHERE status = 'CHECKED_IN';
CREATE INDEX IF NOT EXISTS idx_attendance_checked_out ON public.attendance(id) WHERE status = 'CHECKED_OUT';

-- Add indexes for foreign key columns that don't have them yet
CREATE INDEX IF NOT EXISTS idx_user_token_user_id_active ON public.user_token(user_id) WHERE is_revoked = false;
CREATE INDEX IF NOT EXISTS idx_password_reset_user_id_valid ON public.password_reset(user_id) WHERE is_used = false;

-- Ensure proper cascade behavior for critical relationships
-- Most foreign keys already have CASCADE, but let's verify the important ones

-- Add comments to tables for documentation
COMMENT ON TABLE public."users" IS 'Main user authentication table';
COMMENT ON TABLE public.user_profile IS 'Extended user profile information';
COMMENT ON TABLE public.event IS 'Community events and gatherings';
COMMENT ON TABLE public.post IS 'Community posts and announcements';
COMMENT ON TABLE public.service IS 'Regular church services';
COMMENT ON TABLE public.kids_service IS 'Children''s services and programs';
COMMENT ON TABLE public.kid IS 'Children registered in the system';
COMMENT ON TABLE public.attendance IS 'Attendance tracking for all events and services';
COMMENT ON TABLE public.password_reset IS 'Password reset token management';
COMMENT ON TABLE public.user_token IS 'JWT token management';

-- Add comments to important columns
COMMENT ON COLUMN public."users".auth_provider IS 'Authentication provider: LOCAL, GOOGLE, FACEBOOK';
COMMENT ON COLUMN public.attendance.event_type IS 'Type of event: EVENT, SERVICE, KIDS_SERVICE';
COMMENT ON COLUMN public.attendance.event_id IS 'ID of the event/service (polymorphic reference)';
COMMENT ON COLUMN public.kid.family_id IS 'Reference to the family (user_profile) this child belongs to';