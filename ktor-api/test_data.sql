-- Test data for attendance system
-- Run these SQL statements to create test data for your attendance system

-- 1. Insert test users (if they don't exist)
INSERT INTO user (id, email, password, salt, verified, created_at, auth_provider) VALUES
(1, 'admin@church.com', 'hashed_password', 'salt123', true, '2024-01-01 10:00:00', 'LOCAL'),
(2, 'john.doe@email.com', 'hashed_password', 'salt456', true, '2024-01-01 10:00:00', 'LOCAL'),
(3, 'jane.smith@email.com', 'hashed_password', 'salt789', true, '2024-01-01 10:00:00', 'LOCAL'),
(4, 'mike.johnson@email.com', 'hashed_password', 'salt101', true, '2024-01-01 10:00:00', 'LOCAL'),
(5, 'sarah.wilson@email.com', 'hashed_password', 'salt202', true, '2024-01-01 10:00:00', 'LOCAL')
ON CONFLICT (id) DO NOTHING;

-- 2. Insert user profiles
INSERT INTO user_profile (id, user_id, first_name, last_name, email, phone, joined_at, image_path, is_admin) VALUES
(1, 1, 'Admin', 'User', 'admin@church.com', '+1234567890', '2024-01-01 10:00:00', '', true),
(2, 2, 'John', 'Doe', 'john.doe@email.com', '+1234567891', '2024-01-01 10:00:00', '', false),
(3, 3, 'Jane', 'Smith', 'jane.smith@email.com', '+1234567892', '2024-01-01 10:00:00', '', false),
(4, 4, 'Mike', 'Johnson', 'mike.johnson@email.com', '+1234567893', '2024-01-01 10:00:00', '', false),
(5, 5, 'Sarah', 'Wilson', 'sarah.wilson@email.com', '+1234567894', '2024-01-01 10:00:00', '', false)
ON CONFLICT (id) DO NOTHING;

-- 3. Insert test services
INSERT INTO service (id, name, description, start_time, end_time, location, service_type, is_active, created_at) VALUES
(1, 'Sunday Morning Service', 'Main worship service on Sunday morning', '2024-07-28 09:00:00', '2024-07-28 11:00:00', 'Main Sanctuary', 'REGULAR', true, '2024-07-20 10:00:00'),
(2, 'Sunday Evening Service', 'Evening worship service', '2024-07-28 18:00:00', '2024-07-28 20:00:00', 'Main Sanctuary', 'REGULAR', true, '2024-07-20 10:00:00'),
(3, 'Youth Service', 'Service for young people', '2024-07-26 19:00:00', '2024-07-26 21:00:00', 'Youth Hall', 'YOUTH', true, '2024-07-20 10:00:00'),
(4, 'Prayer Meeting', 'Weekly prayer meeting', '2024-07-24 19:00:00', '2024-07-24 20:30:00', 'Prayer Room', 'PRAYER', true, '2024-07-20 10:00:00'),
(5, 'Special Christmas Service', 'Christmas celebration service', '2024-12-25 10:00:00', '2024-12-25 12:00:00', 'Main Sanctuary', 'SPECIAL', true, '2024-07-20 10:00:00')
ON CONFLICT (id) DO NOTHING;

-- 4. Insert attendance records for service ID 2 (Sunday Evening Service)
INSERT INTO attendance (id, event_type, event_id, user_id, kid_id, checked_in_by, check_in_time, check_out_time, checked_out_by, status, notes, created_at) VALUES
(1, 'SERVICE', 2, 2, NULL, 1, '2024-07-28 17:45:00', '2024-07-28 20:15:00', 1, 'CHECKED_OUT', 'Regular attendee', '2024-07-28 17:45:00'),
(2, 'SERVICE', 2, 3, NULL, 1, '2024-07-28 17:50:00', NULL, NULL, 'CHECKED_IN', 'First time visitor', '2024-07-28 17:50:00'),
(3, 'SERVICE', 2, 4, NULL, 1, '2024-07-28 18:05:00', NULL, NULL, 'CHECKED_IN', 'Volunteer helper', '2024-07-28 18:05:00'),
(4, 'SERVICE', 2, 5, NULL, 1, '2024-07-28 18:10:00', '2024-07-28 19:45:00', 1, 'CHECKED_OUT', 'Left early for family commitment', '2024-07-28 18:10:00')
ON CONFLICT (id) DO NOTHING;

-- 5. Insert additional attendance records for other services (for testing)
INSERT INTO attendance (id, event_type, event_id, user_id, kid_id, checked_in_by, check_in_time, check_out_time, checked_out_by, status, notes, created_at) VALUES
(5, 'SERVICE', 1, 2, NULL, 1, '2024-07-28 08:45:00', '2024-07-28 11:15:00', 1, 'CHECKED_OUT', 'Sunday morning regular', '2024-07-28 08:45:00'),
(6, 'SERVICE', 1, 3, NULL, 1, '2024-07-28 08:50:00', '2024-07-28 11:10:00', 1, 'CHECKED_OUT', 'Helped with communion', '2024-07-28 08:50:00'),
(7, 'SERVICE', 1, 4, NULL, 1, '2024-07-28 09:00:00', NULL, NULL, 'CHECKED_IN', 'Still in service', '2024-07-28 09:00:00'),
(8, 'SERVICE', 3, 4, NULL, 1, '2024-07-26 18:55:00', '2024-07-26 21:05:00', 1, 'CHECKED_OUT', 'Youth leader', '2024-07-26 18:55:00'),
(9, 'SERVICE', 3, 5, NULL, 1, '2024-07-26 19:10:00', '2024-07-26 21:00:00', 1, 'CHECKED_OUT', 'Youth participant', '2024-07-26 19:10:00')
ON CONFLICT (id) DO NOTHING;

-- 6. Reset sequence counters (if using PostgreSQL)
-- SELECT setval('user_id_seq', (SELECT MAX(id) FROM user));
-- SELECT setval('user_profile_id_seq', (SELECT MAX(id) FROM user_profile));
-- SELECT setval('service_id_seq', (SELECT MAX(id) FROM service));
-- SELECT setval('attendance_id_seq', (SELECT MAX(id) FROM attendance));

-- For SQLite, sequences are handled automatically

-- 7. Insert test kids
INSERT INTO kid (id, family_id, first_name, last_name, date_of_birth, allergies, notes) VALUES
(1, 2, 'Emma', 'Doe', '2018-05-15', 'Peanuts', 'Very active child'),
(2, 3, 'Liam', 'Smith', '2019-08-22', NULL, 'Loves drawing'),
(3, 4, 'Sophia', 'Johnson', '2017-12-03', 'Dairy', 'Shy but friendly'),
(4, 5, 'Noah', 'Wilson', '2020-03-10', NULL, 'Energetic toddler'),
(5, 2, 'Oliver', 'Doe', '2016-09-18', NULL, 'John Doe second child')
ON CONFLICT (id) DO NOTHING;

-- 8. Insert test kids services
INSERT INTO kids_service (id, service_id, name, description, age_group_min, age_group_max, max_capacity, location, is_active, created_at) VALUES
(1, 1, 'Little Lambs (0-3)', 'Nursery service for toddlers', 0, 3, 15, 'Nursery Room A', true, '2024-07-20 10:00:00'),
(2, 1, 'Mighty Lions (4-7)', 'Kids service for preschool age', 4, 7, 25, 'Kids Hall B', true, '2024-07-20 10:00:00'),
(3, 1, 'Super Heroes (8-12)', 'Kids service for school age', 8, 12, 30, 'Kids Hall C', true, '2024-07-20 10:00:00'),
(4, 2, 'Evening Kids (4-12)', 'Evening kids service', 4, 12, 20, 'Kids Hall B', true, '2024-07-20 10:00:00'),
(10, 1, 'Adventure Club (5-10)', 'Special adventure-themed kids service', 5, 10, 20, 'Adventure Room', true, '2024-07-20 10:00:00')
ON CONFLICT (id) DO NOTHING;

-- 9. Insert attendance records for kids service ID 2 (Mighty Lions)
INSERT INTO attendance (id, event_type, event_id, user_id, kid_id, checked_in_by, check_in_time, check_out_time, checked_out_by, status, notes, created_at) VALUES
(10, 'KIDS_SERVICE', 2, NULL, 1, 1, '2024-07-28 08:45:00', '2024-07-28 11:15:00', 1, 'CHECKED_OUT', 'Had a great time in class', '2024-07-28 08:45:00'),
(11, 'KIDS_SERVICE', 2, NULL, 2, 1, '2024-07-28 08:50:00', NULL, NULL, 'CHECKED_IN', 'First time visitor', '2024-07-28 08:50:00'),
(12, 'KIDS_SERVICE', 2, NULL, 3, 1, '2024-07-28 09:00:00', '2024-07-28 11:10:00', 1, 'CHECKED_OUT', 'Participated well in activities', '2024-07-28 09:00:00')
ON CONFLICT (id) DO NOTHING;

-- 10. Insert additional kids attendance for other services
INSERT INTO attendance (id, event_type, event_id, user_id, kid_id, checked_in_by, check_in_time, check_out_time, checked_out_by, status, notes, created_at) VALUES
(13, 'KIDS_SERVICE', 1, NULL, 4, 1, '2024-07-28 08:55:00', '2024-07-28 11:05:00', 1, 'CHECKED_OUT', 'Enjoyed nursery time', '2024-07-28 08:55:00'),
(14, 'KIDS_SERVICE', 3, NULL, 5, 1, '2024-07-28 09:05:00', NULL, NULL, 'CHECKED_IN', 'Helping with younger kids', '2024-07-28 09:05:00'),
(15, 'KIDS_SERVICE', 10, NULL, 1, 1, '2024-07-28 09:00:00', '2024-07-28 11:30:00', 1, 'CHECKED_OUT', 'Loved the adventure activities', '2024-07-28 09:00:00'),
(16, 'KIDS_SERVICE', 10, NULL, 2, 1, '2024-07-28 09:10:00', NULL, NULL, 'CHECKED_IN', 'Very engaged in treasure hunt', '2024-07-28 09:10:00'),
(17, 'KIDS_SERVICE', 10, NULL, 3, 1, '2024-07-28 09:15:00', '2024-07-28 11:25:00', 1, 'CHECKED_OUT', 'Great teamwork skills', '2024-07-28 09:15:00')
ON CONFLICT (id) DO NOTHING;

-- Verification queries to check the data:

-- Check service data
-- SELECT * FROM service WHERE id = 2;
-- SELECT * FROM attendance WHERE event_type = 'SERVICE' AND event_id = 2;

-- Check kids service data  
-- SELECT * FROM kids_service WHERE id = 2;
-- SELECT * FROM attendance WHERE event_type = 'KIDS_SERVICE' AND event_id = 2;

-- Full attendance query for services
-- SELECT 
--     a.id,
--     a.event_type,
--     a.event_id,
--     a.check_in_time,
--     a.check_out_time,
--     a.status,
--     a.notes,
--     up.first_name || ' ' || up.last_name as attendee_name,
--     s.name as service_name
-- FROM attendance a
-- LEFT JOIN user_profile up ON a.user_id = up.user_id
-- LEFT JOIN service s ON a.event_id = s.id
-- WHERE a.event_type = 'SERVICE' AND a.event_id = 2;

-- Full attendance query for kids services
-- SELECT 
--     a.id,
--     a.event_type,
--     a.event_id,
--     a.check_in_time,
--     a.check_out_time,
--     a.status,
--     a.notes,
--     k.first_name || ' ' || k.last_name as kid_name,
--     ks.name as kids_service_name,
--     up.first_name || ' ' || up.last_name as checked_in_by_name
-- FROM attendance a
-- LEFT JOIN kid k ON a.kid_id = k.id
-- LEFT JOIN kids_service ks ON a.event_id = ks.id
-- LEFT JOIN user_profile up ON a.checked_in_by = up.user_id
-- WHERE a.event_type = 'KIDS_SERVICE' AND a.event_id = 2;