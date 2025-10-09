-- Update kid_attendance table to support QR code-based check-in system
-- Add reference to check_in_request and track staff approval

-- Add check_in_request_id column as foreign key
ALTER TABLE kid_attendance 
ADD COLUMN check_in_request_id BIGINT;

-- Add approved_by_staff column to track which staff member approved the check-in
ALTER TABLE kid_attendance 
ADD COLUMN approved_by_staff VARCHAR(255);

-- Add foreign key constraint
ALTER TABLE kid_attendance 
ADD CONSTRAINT fk_kid_attendance_check_in_request 
FOREIGN KEY (check_in_request_id) REFERENCES check_in_request(id) ON DELETE SET NULL;

-- Create index for check_in_request_id for performance
CREATE INDEX idx_kid_attendance_check_in_request ON kid_attendance(check_in_request_id);

-- Add comments
COMMENT ON COLUMN kid_attendance.check_in_request_id IS 'Reference to the check-in request if this attendance was created via QR code verification';
COMMENT ON COLUMN kid_attendance.approved_by_staff IS 'Name or identifier of the staff member who approved the QR code check-in';
