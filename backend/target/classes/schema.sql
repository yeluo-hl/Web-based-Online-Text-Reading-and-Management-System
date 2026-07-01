-- Create database if not exists
CREATE DATABASE IF NOT EXISTS editor CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use the database
USE editor;

-- Drop tables if they exist (for clean reinitialization)
DROP TABLE IF EXISTS documents;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    userId VARCHAR(50) UNIQUE NOT NULL COMMENT 'User account name',
    email VARCHAR(100) UNIQUE NOT NULL COMMENT 'User email',
    name VARCHAR(100) NOT NULL COMMENT 'User display name',
    password VARCHAR(255) NOT NULL COMMENT 'Encrypted password',
    role ENUM('user', 'admin') DEFAULT 'user' COMMENT 'User role',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
    INDEX idx_userId (userid),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User accounts table';

-- Create documents table
CREATE TABLE documents (
    id VARCHAR(36) PRIMARY KEY COMMENT 'Document unique ID',
    user_id INT NOT NULL COMMENT 'Owner user ID',
    title VARCHAR(255) DEFAULT 'Untitled Document' COMMENT 'Document title',
    content LONGTEXT COMMENT 'Document content (HTML format)',
    word_count INT DEFAULT 0 COMMENT 'Word count in document',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_updated_at (updated_at),
    INDEX idx_title (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Documents table';

-- Insert default admin user (password: admin123)
INSERT INTO users (userId, email, name, password, role)
VALUES ('admin', 'admin@example.com', 'Administrator', '123456', 'admin')
ON DUPLICATE KEY UPDATE name = name;

-- Insert sample user for testing (password: test123)
INSERT INTO users (userId, email, name, password, role)
VALUES ('testuser', 'test@example.com', 'Test User', '123456', 'user')
ON DUPLICATE KEY UPDATE name = name;

-- Insert sample documents for testing
INSERT INTO documents (id, user_id, title, content, word_count) 
VALUES 
    ('doc1', 1, 'Welcome Document', '<h1>Welcome to Text Editor</h1><p>This is a sample document created for testing purposes.</p>', 12),
    ('doc2', 1, 'Sample Article', '<h2>Sample Article</h2><p>This is another sample document with more content.</p><ul><li>Item 1</li><li>Item 2</li></ul>', 18),
    ('doc3', 2, 'User Document', '<p>This is a document created by the test user.</p><p>It contains some sample text.</p>', 14)
ON DUPLICATE KEY UPDATE content = VALUES(content), word_count = VALUES(word_count);

-- Show table structures
DESCRIBE users;
DESCRIBE documents;

-- Show inserted data
SELECT 'Users table data:' as info;
SELECT id, userId, email, name, role, created_at FROM users;

SELECT 'Documents table data:' as info;
SELECT id, user_id, title, word_count, created_at FROM documents;
