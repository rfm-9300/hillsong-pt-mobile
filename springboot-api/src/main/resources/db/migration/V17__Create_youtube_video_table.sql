-- Create youtube_video table
CREATE TABLE IF NOT EXISTS youtube_video (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    video_url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL
);

-- Create index for efficient querying
CREATE INDEX IF NOT EXISTS idx_active_display_order ON youtube_video (active, display_order);

-- Insert sample YouTube videos
INSERT INTO youtube_video (title, description, video_url, thumbnail_url, display_order, active) VALUES
('Welcome to Our Church', 'Introduction to our church community and mission', 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', 'https://img.youtube.com/vi/dQw4w9WgXcQ/maxresdefault.jpg', 1, TRUE),
('Sunday Service Highlights', 'Highlights from our recent Sunday service', 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', 'https://img.youtube.com/vi/dQw4w9WgXcQ/maxresdefault.jpg', 2, TRUE),
('Youth Ministry Activities', 'See what our youth ministry is up to', 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', 'https://img.youtube.com/vi/dQw4w9WgXcQ/maxresdefault.jpg', 3, TRUE);
