-- Run with a MySQL administrator account. Existing data is preserved.
CREATE DATABASE IF NOT EXISTS campus_events CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_events;
CREATE TABLE IF NOT EXISTS registrations (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 student_id VARCHAR(20) NOT NULL,
 student_name VARCHAR(80) NOT NULL,
 event_name VARCHAR(40) NOT NULL,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT uq_student_event UNIQUE (student_id,event_name),
 INDEX idx_event_id (event_name,id)
);
-- Create an application user yourself using a strong password, then grant:
-- GRANT SELECT, INSERT ON campus_events.* TO 'campus_app'@'localhost';
