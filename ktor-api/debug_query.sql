-- Debug queries to check the data structure

-- Check if service ID 2 exists
SELECT * FROM service WHERE id = 2;

-- Check if attendance records exist for service ID 2
SELECT * FROM attendance WHERE event_type = 'SERVICE' AND event_id = 2;

-- Check user profiles for the users in attendance
SELECT up.* FROM user_profile up 
JOIN attendance a ON up.user_id = a.user_id 
WHERE a.event_type = 'SERVICE' AND a.event_id = 2;

-- Full join to see what the backend query should return
SELECT 
    a.id as attendance_id,
    a.event_type,
    a.event_id,
    a.user_id,
    a.check_in_time,
    a.check_out_time,
    a.status,
    a.notes,
    up.first_name,
    up.last_name,
    up.email,
    s.name as service_name
FROM attendance a
LEFT JOIN user_profile up ON a.user_id = up.user_id
LEFT JOIN service s ON a.event_id = s.id
WHERE a.event_type = 'SERVICE' AND a.event_id = 2;