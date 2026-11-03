-- Apply compatibility changes to an existing LevTicket database.
-- Fresh databases receive this column from init.sql.
USE bidpass;

SET @column_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ticket_image'
      AND COLUMN_NAME = 'is_private'
);

SET @add_column_sql = IF(
    @column_exists = 0,
    'ALTER TABLE ticket_image ADD COLUMN is_private TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''0 = public cover shown in listings, 1 = private ticket photo revealed only to winner/seller''',
    'SELECT 1'
);

PREPARE add_column_statement FROM @add_column_sql;
EXECUTE add_column_statement;
DEALLOCATE PREPARE add_column_statement;

CREATE TABLE IF NOT EXISTS notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(40) NOT NULL,
    title VARCHAR(150) NOT NULL,
    message VARCHAR(500) NOT NULL,
    related_id BIGINT NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_notification_user_read (user_id, is_read, create_time)
) ENGINE=InnoDB;

-- Saved / watched tickets, powers the "Saved" page and outbid fan-out.
CREATE TABLE IF NOT EXISTS watchlist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    ticket_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_watchlist_user_ticket (user_id, ticket_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (ticket_id) REFERENCES ticket(id) ON DELETE CASCADE,
    INDEX idx_watchlist_user (user_id, create_time)
) ENGINE=InnoDB;

-- Private buyer <-> seller messages, powers the chat window and the Messages inbox.
CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ticket_id BIGINT NULL,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ticket_id) REFERENCES ticket(id) ON DELETE SET NULL,
    FOREIGN KEY (sender_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_chat_pair (sender_id, receiver_id, create_time),
    INDEX idx_chat_receiver_read (receiver_id, is_read),
    INDEX idx_chat_ticket (ticket_id)
) ENGINE=InnoDB;
