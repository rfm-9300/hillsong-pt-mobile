-- Create encounter table
CREATE TABLE IF NOT EXISTS encounter (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    date TIMESTAMP NOT NULL,
    location VARCHAR(255) NOT NULL,
    organizer_id BIGINT NOT NULL,
    image_path VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_encounter_organizer FOREIGN KEY (organizer_id) REFERENCES user_profile(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_encounter_date ON encounter(date);
CREATE INDEX IF NOT EXISTS idx_encounter_organizer ON encounter(organizer_id);
CREATE INDEX IF NOT EXISTS idx_encounter_title ON encounter(title);
CREATE INDEX IF NOT EXISTS idx_encounter_location ON encounter(location);
