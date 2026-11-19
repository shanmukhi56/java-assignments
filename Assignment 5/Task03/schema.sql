-- =========================================================================
-- CSA09 ASSIGNMENT 5: TASK B3 & TASK B4 DATABASE SCHEMA
-- Campus Transport Management System
-- =========================================================================

CREATE DATABASE IF NOT EXISTS campus_transport;
USE campus_transport;

DROP TABLE IF EXISTS bus_pass;
DROP TABLE IF EXISTS canteen_wallet;
DROP TABLE IF EXISTS hostel_wallet;
DROP TABLE IF EXISTS users;

-- 1. Bus Pass Table (Indexed for High Concurrency and Fast Route Lookup)
CREATE TABLE bus_pass (
    pass_id VARCHAR(20) PRIMARY KEY,
    reg_no VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    route_no INT NOT NULL,
    boarding_stop VARCHAR(100) DEFAULT 'Main Gate',
    validity_date DATE DEFAULT (CURRENT_DATE + INTERVAL 1 YEAR),
    amount DECIMAL(10, 2) NOT NULL,
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    campus_id INT DEFAULT 1,
    INDEX idx_route (route_no),
    INDEX idx_reg_no (reg_no),
    INDEX idx_campus (campus_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Users Table for Authentication & Injection Testing
CREATE TABLE users (
    id VARCHAR(50) PRIMARY KEY,
    pwd VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO users (id, pwd, role) VALUES 
('admin', 'AdminSecret@2026', 'ADMIN'),
('counter01', 'Counter#2026', 'OPERATOR'),
('student_arun', 'Arun@3800', 'STUDENT');

-- 3. Wallet Tables for Transaction Verification (Task A5.1)
CREATE TABLE hostel_wallet (
    student_id VARCHAR(20) PRIMARY KEY,
    balance DECIMAL(10, 2) NOT NULL CHECK (balance >= 0)
) ENGINE=InnoDB;

CREATE TABLE canteen_wallet (
    student_id VARCHAR(20) PRIMARY KEY,
    balance DECIMAL(10, 2) NOT NULL CHECK (balance >= 0)
) ENGINE=InnoDB;

INSERT INTO hostel_wallet (student_id, balance) VALUES ('192311001', 2500.00);
INSERT INTO canteen_wallet (student_id, balance) VALUES ('192311001', 350.00);
