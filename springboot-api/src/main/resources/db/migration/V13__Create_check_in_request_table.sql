-- Create check_in_request table for QR code-based check-in system
CREATE TABLE check_in_request (
    id BIGSERIAL PRIMARY KEY,
    kid_id BIGINT NOT NULL,
    kids_service_id BIGINT NOT NULL,
    requested_by_user_id BIGINT NOT NULL,
    token VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    processed_by_user_id BIGINT,
    processed_at TIMESTAMP,
    rejection_reason TEXT,
    notes TEXT,
    
    -- Foreign key constraints
    CONSTRAINT fk_check_in_request_kid FOREIGN KEY (kid_id) REFERENCES kid(id) ON DELETE CASCADE,
    CONSTRAINT fk_check_in_request_kids_service FOREIGN KEY (kids_service_id) REFERENCES kids_service(id) ON DELETE CASCADE,
    CONSTRAINT fk_check_in_request_requested_by FOREIGN KEY (requested_by_user_id) REFERENCES user_profile(id) ON DELETE CASCADE,
    CONSTRAINT fk_check_in_request_processed_by FOREIGN KEY (processed_by_user_id) REFERENCES user_profile(id) ON DELETE SET NULL,
    
    -- Check constraint for status values
    CONSTRAINT chk_check_in_request_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'EXPIRED', 'CANCELLED'))
);

-- Create indexes for performance optimization
CREATE INDEX idx_check_in_request_token ON check_in_request(token);
CREATE INDEX idx_check_in_request_status ON check_in_request(status);
CREATE INDEX idx_check_in_request_expires_at ON check_in_request(expires_at);
CREATE INDEX idx_check_in_request_kid_service ON check_in_request(kid_id, kids_service_id);
CREATE INDEX idx_check_in_request_requested_by ON check_in_request(requested_by_user_id);
CREATE INDEX idx_check_in_request_kids_service ON check_in_request(kids_service_id);

-- Add comment to table
COMMENT ON TABLE check_in_request IS 'Stores QR code-based check-in requests that require staff verification';
COMMENT ON COLUMN check_in_request.token IS 'Unique secure token encoded in QR code, expires after configured time';
COMMENT ON COLUMN check_in_request.status IS 'Current status: PENDING, APPROVED, REJECTED, EXPIRED, or CANCELLED';
COMMENT ON COLUMN check_in_request.expires_at IS 'Timestamp when the check-in request expires (default 15 minutes from creation)';
