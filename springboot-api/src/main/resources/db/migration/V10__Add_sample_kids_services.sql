-- V10__Add_sample_kids_services.sql
-- Fix kids_service table structure and add sample data

-- First, add missing columns to kids_service table
ALTER TABLE public.kids_service 
ADD COLUMN IF NOT EXISTS day_of_week VARCHAR(15),
ADD COLUMN IF NOT EXISTS start_time TIME,
ADD COLUMN IF NOT EXISTS end_time TIME,
ADD COLUMN IF NOT EXISTS volunteer_to_child_ratio VARCHAR(10),
ADD COLUMN IF NOT EXISTS special_requirements TEXT,
ADD COLUMN IF NOT EXISTS notes TEXT,
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT NOW(),
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- Add check constraint for day_of_week (only if it doesn't exist)
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'chk_kids_service_day_of_week' 
        AND table_name = 'kids_service'
    ) THEN
        ALTER TABLE public.kids_service 
        ADD CONSTRAINT chk_kids_service_day_of_week 
        CHECK (day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'));
    END IF;
END $$;

-- Insert sample kids services
INSERT INTO public.kids_service (
    name, 
    description, 
    day_of_week, 
    start_time, 
    end_time, 
    location, 
    max_capacity, 
    min_age, 
    max_age, 
    is_active,
    requires_pre_registration,
    check_in_starts_minutes_before,
    check_in_ends_minutes_after,
    special_requirements,
    notes
) VALUES 
(
    'Little Lambs (Nursery)',
    'A safe and nurturing environment for our youngest children with age-appropriate activities and care.',
    'SUNDAY',
    '09:00:00',
    '10:30:00',
    'Nursery Room A',
    15,
    0,
    2,
    true,
    false,
    30,
    15,
    'Diaper changing facilities available. Parents must provide bottles and snacks.',
    'Trained childcare volunteers with background checks.'
),
(
    'Tiny Tots (Preschool)',
    'Interactive Bible stories, songs, and crafts designed for preschool-aged children.',
    'SUNDAY',
    '09:00:00',
    '10:30:00',
    'Preschool Room B',
    20,
    3,
    5,
    true,
    false,
    30,
    15,
    'Potty-trained children only. Allergy-free snacks provided.',
    'Engaging activities with qualified early childhood educators.'
),
(
    'Kids Church (Elementary)',
    'Fun and engaging worship experience with age-appropriate teaching and activities.',
    'SUNDAY',
    '09:00:00',
    '10:30:00',
    'Kids Church Main Hall',
    40,
    6,
    11,
    true,
    false,
    30,
    15,
    'Children should be able to follow basic instructions independently.',
    'Interactive worship, Bible lessons, and group activities.'
),
(
    'Youth Connect (Middle School)',
    'Dynamic worship and relevant teaching designed specifically for middle school students.',
    'SUNDAY',
    '09:00:00',
    '10:30:00',
    'Youth Room C',
    25,
    12,
    14,
    true,
    false,
    30,
    15,
    'Age-appropriate discussions about faith and life challenges.',
    'Youth pastors and trained volunteers lead engaging sessions.'
),
(
    'Wednesday Kids Club',
    'Midweek program with games, Bible study, and fellowship for elementary-aged children.',
    'WEDNESDAY',
    '18:30:00',
    '20:00:00',
    'Fellowship Hall',
    30,
    6,
    11,
    true,
    true,
    30,
    15,
    'Dinner provided. Registration required by Tuesday.',
    'Family-style dinner followed by age-appropriate activities.'
),
(
    'Friday Night Youth',
    'Weekly youth gathering with worship, teaching, and social activities.',
    'FRIDAY',
    '19:00:00',
    '21:00:00',
    'Youth Center',
    50,
    12,
    17,
    true,
    false,
    30,
    15,
    'Snacks and drinks provided. Parent pickup required.',
    'Contemporary worship and relevant biblical teaching for teens.'
);

-- Insert age groups for each service
INSERT INTO public.kids_service_age_groups (kids_service_id, age_group) VALUES
-- Little Lambs (Nursery)
(1, 'NURSERY'),
-- Tiny Tots (Preschool)  
(2, 'PRESCHOOL'),
-- Kids Church (Elementary)
(3, 'ELEMENTARY_LOWER'),
(3, 'ELEMENTARY_UPPER'),
-- Youth Connect (Middle School)
(4, 'MIDDLE_SCHOOL'),
-- Wednesday Kids Club
(5, 'ELEMENTARY_LOWER'),
(5, 'ELEMENTARY_UPPER'),
-- Friday Night Youth
(6, 'MIDDLE_SCHOOL'),
(6, 'HIGH_SCHOOL');

-- Add some sample kids (assuming user_profile with id 4 exists from the logs)
INSERT INTO public.kid (
    first_name,
    last_name,
    date_of_birth,
    gender,
    primary_parent_id,
    emergency_contact_name,
    emergency_contact_phone,
    medical_notes,
    allergies,
    is_active
) VALUES 
(
    'Emma',
    'Martins',
    '2020-03-15',
    'FEMALE',
    4,
    'Maria Silva',
    '+351 912 345 678',
    'No known medical conditions',
    'None',
    true
),
(
    'Lucas',
    'Martins', 
    '2018-07-22',
    'MALE',
    4,
    'Maria Silva',
    '+351 912 345 678',
    'Mild asthma - inhaler available if needed',
    'Peanuts',
    true
),
(
    'Sofia',
    'Martins',
    '2015-11-08',
    'FEMALE', 
    4,
    'Maria Silva',
    '+351 912 345 678',
    'No known medical conditions',
    'Dairy products',
    true
);