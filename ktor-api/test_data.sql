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

-- Verification queries to check the data:
-- SELECT * FROM service WHERE id = 2;
-- SELECT * FROM attendance WHERE event_type = 'SERVICE' AND event_id = 2;
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