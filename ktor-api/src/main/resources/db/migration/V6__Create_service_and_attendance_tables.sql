-- V6__Create_service_and_attendance_tables.sql
-- Create service, attendance, and kids-related tables

-- Create service table
CREATE TABLE IF NOT EXISTS public.service (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    service_type VARCHAR(30) NOT NULL,
    day_of_week VARCHAR(15) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    location VARCHAR(255) NOT NULL,
    leader_id INTEGER,
    max_capacity INTEGER,
    is_active BOOLEAN NOT NULL DEFAULT true,
    requires_registration BOOLEAN NOT NULL DEFAULT false,
    registration_deadline_hours INTEGER,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT fk_service_leader FOREIGN KEY (leader_id) REFERENCES public.user_profile (id) ON DELETE SET NULL,
    CONSTRAINT chk_service_type CHECK (service_type IN ('SUNDAY_WORSHIP', 'BIBLE_STUDY', 'PRAYER_MEETING', 'YOUTH_SERVICE', 'SMALL_GROUP', 'SPECIAL_EVENT', 'COMMUNITY_OUTREACH', 'DISCIPLESHIP', 'WORSHIP_PRACTICE', 'LEADERSHIP_MEETING', 'OTHER')),
    CONSTRAINT chk_day_of_week CHECK (day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'))
);

-- Create service_registration junction table
CREATE TABLE IF NOT EXISTS public.service_registration (
    service_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    PRIMARY KEY (service_id, user_id),
    CONSTRAINT fk_service_registration_service FOREIGN KEY (service_id) REFERENCES public.service (id) ON DELETE CASCADE,
    CONSTRAINT fk_service_registration_user FOREIGN KEY (user_id) REFERENCES public."users" (id) ON DELETE CASCADE
);

-- Create kid table
CREATE TABLE IF NOT EXISTS public.kid (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(10),
    primary_parent_id INTEGER NOT NULL,
    secondary_parent_id INTEGER,
    emergency_contact_name VARCHAR(255),
    emergency_contact_phone VARCHAR(20),
    medical_notes TEXT,
    allergies TEXT,
    special_needs TEXT,
    pickup_authorization TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT fk_kid_primary_parent FOREIGN KEY (primary_parent_id) REFERENCES public.user_profile (id) ON DELETE CASCADE,
    CONSTRAINT fk_kid_secondary_parent FOREIGN KEY (secondary_parent_id) REFERENCES public.user_profile (id) ON DELETE SET NULL,
    CONSTRAINT chk_gender CHECK (gender IN ('MALE', 'FEMALE', 'OTHER'))
);

-- Create kids_service table
CREATE TABLE IF NOT EXISTS public.kids_service (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    day_of_week VARCHAR(15) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    location VARCHAR(255) NOT NULL,
    leader_id INTEGER,
    max_capacity INTEGER NOT NULL,
    min_age INTEGER NOT NULL,
    max_age INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    requires_pre_registration BOOLEAN NOT NULL DEFAULT false,
    check_in_starts_minutes_before INTEGER DEFAULT 30,
    check_in_ends_minutes_after INTEGER DEFAULT 15,
    volunteer_to_child_ratio VARCHAR(10),
    special_requirements TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT fk_kids_service_leader FOREIGN KEY (leader_id) REFERENCES public.user_profile (id) ON DELETE SET NULL,
    CONSTRAINT chk_kids_service_day_of_week CHECK (day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')),
    CONSTRAINT chk_kids_service_ages CHECK (min_age >= 0 AND max_age >= min_age),
    CONSTRAINT chk_kids_service_capacity CHECK (max_capacity > 0)
);

-- Create kids_service_age_groups table for ElementCollection
CREATE TABLE IF NOT EXISTS public.kids_service_age_groups (
    kids_service_id INTEGER NOT NULL,
    age_group VARCHAR(20) NOT NULL,
    CONSTRAINT fk_kids_service_age_groups_service FOREIGN KEY (kids_service_id) REFERENCES public.kids_service (id) ON DELETE CASCADE,
    CONSTRAINT chk_age_group CHECK (age_group IN ('NURSERY', 'PRESCHOOL', 'ELEMENTARY_LOWER', 'ELEMENTARY_UPPER', 'MIDDLE_SCHOOL', 'HIGH_SCHOOL', 'ADULT'))
);

-- Create kids_service_enrollment junction table
CREATE TABLE IF NOT EXISTS public.kids_service_enrollment (
    kids_service_id INTEGER NOT NULL,
    kid_id INTEGER NOT NULL,
    PRIMARY KEY (kids_service_id, kid_id),
    CONSTRAINT fk_kids_service_enrollment_service FOREIGN KEY (kids_service_id) REFERENCES public.kids_service (id) ON DELETE CASCADE,
    CONSTRAINT fk_kids_service_enrollment_kid FOREIGN KEY (kid_id) REFERENCES public.kid (id) ON DELETE CASCADE
);

-- Create kids_service_volunteers junction table
CREATE TABLE IF NOT EXISTS public.kids_service_volunteers (
    kids_service_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    PRIMARY KEY (kids_service_id, user_id),
    CONSTRAINT fk_kids_service_volunteers_service FOREIGN KEY (kids_service_id) REFERENCES public.kids_service (id) ON DELETE CASCADE,
    CONSTRAINT fk_kids_service_volunteers_user FOREIGN KEY (user_id) REFERENCES public."users" (id) ON DELETE CASCADE
);

-- Create attendance table (updated structure to match JPA entity)
CREATE TABLE IF NOT EXISTS public.attendance (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    event_id INTEGER,
    service_id INTEGER,
    kids_service_id INTEGER,
    attendance_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CHECKED_IN',
    check_in_time TIMESTAMP NOT NULL DEFAULT NOW(),
    check_out_time TIMESTAMP,
    notes TEXT,
    checked_in_by VARCHAR(255),
    checked_out_by VARCHAR(255),
    CONSTRAINT fk_attendance_user FOREIGN KEY (user_id) REFERENCES public."users" (id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_event FOREIGN KEY (event_id) REFERENCES public.event (id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_service FOREIGN KEY (service_id) REFERENCES public.service (id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_kids_service FOREIGN KEY (kids_service_id) REFERENCES public.kids_service (id) ON DELETE CASCADE,
    CONSTRAINT chk_attendance_type CHECK (attendance_type IN ('EVENT', 'SERVICE', 'KIDS_SERVICE')),
    CONSTRAINT chk_attendance_status CHECK (status IN ('CHECKED_IN', 'CHECKED_OUT', 'NO_SHOW', 'CANCELLED')),
    CONSTRAINT chk_attendance_single_reference CHECK (
        (event_id IS NOT NULL AND service_id IS NULL AND kids_service_id IS NULL) OR
        (event_id IS NULL AND service_id IS NOT NULL AND kids_service_id IS NULL) OR
        (event_id IS NULL AND service_id IS NULL AND kids_service_id IS NOT NULL)
    )
);

-- Create kid_attendance table
CREATE TABLE IF NOT EXISTS public.kid_attendance (
    id SERIAL PRIMARY KEY,
    kid_id INTEGER NOT NULL,
    kids_service_id INTEGER NOT NULL,
    check_in_time TIMESTAMP NOT NULL DEFAULT NOW(),
    check_out_time TIMESTAMP,
    checked_in_by VARCHAR(255) NOT NULL,
    checked_out_by VARCHAR(255),
    notes TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'CHECKED_IN',
    CONSTRAINT fk_kid_attendance_kid FOREIGN KEY (kid_id) REFERENCES public.kid (id) ON DELETE CASCADE,
    CONSTRAINT fk_kid_attendance_kids_service FOREIGN KEY (kids_service_id) REFERENCES public.kids_service (id) ON DELETE CASCADE,
    CONSTRAINT chk_kid_attendance_status CHECK (status IN ('CHECKED_IN', 'CHECKED_OUT', 'NO_SHOW', 'CANCELLED'))
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_service_day_of_week ON public.service(day_of_week);
CREATE INDEX IF NOT EXISTS idx_service_start_time ON public.service(start_time);
CREATE INDEX IF NOT EXISTS idx_service_leader_id ON public.service(leader_id);
CREATE INDEX IF NOT EXISTS idx_kid_primary_parent_id ON public.kid(primary_parent_id);
CREATE INDEX IF NOT EXISTS idx_kid_secondary_parent_id ON public.kid(secondary_parent_id);
CREATE INDEX IF NOT EXISTS idx_kid_date_of_birth ON public.kid(date_of_birth);
CREATE INDEX IF NOT EXISTS idx_kids_service_day_of_week ON public.kids_service(day_of_week);
CREATE INDEX IF NOT EXISTS idx_kids_service_start_time ON public.kids_service(start_time);
CREATE INDEX IF NOT EXISTS idx_kids_service_leader_id ON public.kids_service(leader_id);
CREATE INDEX IF NOT EXISTS idx_attendance_user_id ON public.attendance(user_id);
CREATE INDEX IF NOT EXISTS idx_attendance_event_id ON public.attendance(event_id);
CREATE INDEX IF NOT EXISTS idx_attendance_service_id ON public.attendance(service_id);
CREATE INDEX IF NOT EXISTS idx_attendance_kids_service_id ON public.attendance(kids_service_id);
CREATE INDEX IF NOT EXISTS idx_attendance_check_in_time ON public.attendance(check_in_time);
CREATE INDEX IF NOT EXISTS idx_kid_attendance_kid_id ON public.kid_attendance(kid_id);
CREATE INDEX IF NOT EXISTS idx_kid_attendance_kids_service_id ON public.kid_attendance(kids_service_id);
CREATE INDEX IF NOT EXISTS idx_kid_attendance_check_in_time ON public.kid_attendance(check_in_time);