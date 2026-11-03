-- ============================================
-- BidPass Database Schema
-- Team 11 - Leviathan
-- MySQL 8.0
-- ============================================

DROP DATABASE IF EXISTS bidpass;
CREATE DATABASE bidpass CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bidpass;

-- ============================================
-- Table: user
-- ============================================
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    avatar_url VARCHAR(255),
    bio TEXT,
    verification_status ENUM('unverified','email_verified','phone_verified','fully_verified') NOT NULL DEFAULT 'unverified',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================
-- Table: category
-- ============================================
CREATE TABLE category (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    parent_id INT NULL,
    FOREIGN KEY (parent_id) REFERENCES category(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ============================================
-- Table: ticket
-- ============================================
CREATE TABLE ticket (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    seller_id BIGINT NOT NULL,
    category_id INT NOT NULL,
    event_name VARCHAR(150) NOT NULL,
    event_date DATETIME NOT NULL,
    event_location VARCHAR(150) NOT NULL,
    seat_info VARCHAR(100),
    description TEXT,
    tags VARCHAR(255),
    start_price DECIMAL(10,2) NOT NULL,
    status ENUM('active','sold','expired') NOT NULL DEFAULT 'active',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (seller_id) REFERENCES user(id) ON DELETE RESTRICT,
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE RESTRICT,
    CONSTRAINT chk_start_price CHECK (start_price > 0)
) ENGINE=InnoDB;

-- ============================================
-- Table: ticket_image
-- (separate table so each ticket can have multiple images)
-- ============================================
CREATE TABLE ticket_image (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ticket_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    is_private TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0 = public cover shown in listings, 1 = private ticket photo revealed only to winner/seller',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ticket_id) REFERENCES ticket(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================
-- Table: auction
-- ============================================
CREATE TABLE auction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ticket_id BIGINT NOT NULL UNIQUE,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    current_bid DECIMAL(10,2) DEFAULT NULL,
    winner_id BIGINT NULL,
    status ENUM('ongoing','ended','cancelled') NOT NULL DEFAULT 'ongoing',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ticket_id) REFERENCES ticket(id) ON DELETE CASCADE,
    FOREIGN KEY (winner_id) REFERENCES user(id) ON DELETE SET NULL,
    INDEX idx_end_time (end_time),
    CONSTRAINT chk_current_bid CHECK (current_bid IS NULL OR current_bid > 0)
) ENGINE=InnoDB;

-- ============================================
-- Table: bid
-- ============================================
CREATE TABLE bid (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    auction_id BIGINT NOT NULL,
    bidder_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    bid_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (auction_id) REFERENCES auction(id) ON DELETE CASCADE,
    FOREIGN KEY (bidder_id) REFERENCES user(id) ON DELETE RESTRICT,
    INDEX idx_bid_auction_time (auction_id, bid_time),
    CONSTRAINT chk_bid_amount CHECK (amount > 0)
) ENGINE=InnoDB;

-- ============================================
-- Table: orders
-- (named "orders" not "order" -- ORDER is a reserved SQL keyword)
-- ============================================
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    auction_id BIGINT NOT NULL UNIQUE,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    final_price DECIMAL(10,2) NOT NULL,
    status ENUM('pending','confirmed','completed','disputed') NOT NULL DEFAULT 'pending',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (auction_id) REFERENCES auction(id) ON DELETE RESTRICT,
    FOREIGN KEY (buyer_id) REFERENCES user(id) ON DELETE RESTRICT,
    FOREIGN KEY (seller_id) REFERENCES user(id) ON DELETE RESTRICT,
    CONSTRAINT chk_final_price CHECK (final_price > 0)
) ENGINE=InnoDB;

-- ============================================
-- Table: review
-- ============================================
CREATE TABLE review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    reviewee_id BIGINT NOT NULL,
    star_rating TINYINT NOT NULL,
    comment TEXT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES user(id) ON DELETE RESTRICT,
    FOREIGN KEY (reviewee_id) REFERENCES user(id) ON DELETE RESTRICT,
    CONSTRAINT chk_star_rating CHECK (star_rating BETWEEN 1 AND 5),
    UNIQUE KEY uq_review_order_reviewer (order_id, reviewer_id)
) ENGINE=InnoDB;

-- ============================================
-- Table: trust_score
-- ============================================
CREATE TABLE trust_score (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    score DECIMAL(5,2) NOT NULL DEFAULT 100.00,
    total_auctions INT NOT NULL DEFAULT 0,
    successful_auctions INT NOT NULL DEFAULT 0,
    disputes INT NOT NULL DEFAULT 0,
    last_updated DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT chk_trust_score CHECK (score >= 0)
) ENGINE=InnoDB;

-- ============================================
-- Table: complaint
-- ============================================
CREATE TABLE complaint (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    order_id BIGINT NULL,
    description TEXT NOT NULL,
    ai_response TEXT,
    status ENUM('open','resolved') NOT NULL DEFAULT 'open',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ============================================
-- Table: ai_log
-- ============================================
CREATE TABLE ai_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    feature_type ENUM('recommendation','price_advisor','chatbot') NOT NULL,
    prompt_summary TEXT,
    response_summary TEXT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================
-- Table: notification
-- ============================================
CREATE TABLE notification (
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


-- ============================================
-- Table: watchlist
-- (saved tickets -- a user can follow many listings)
-- ============================================
CREATE TABLE watchlist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    ticket_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_watchlist_user_ticket (user_id, ticket_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (ticket_id) REFERENCES ticket(id) ON DELETE CASCADE,
    INDEX idx_watchlist_user (user_id, create_time)
) ENGINE=InnoDB;


-- ============================================
-- Table: chat_message
-- (private buyer <-> seller conversation, optional ticket context)
-- ============================================
CREATE TABLE chat_message (
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


-- ============================================
-- TEST DATA
-- Inserted in dependency order:
-- user -> category -> ticket -> ticket_image ->
-- auction -> bid -> orders -> review ->
-- trust_score -> complaint -> ai_log
-- ============================================

-- user (5 rows)
INSERT INTO user (id, username, email, password_hash, phone, bio, verification_status) VALUES
(1, 'alice_w',  'alice@example.com',  '$2a$10$3AaYtixXrpq9af4HA/GtTeXRPPmVVJd3Ah4O6KgAanXvKNExaAW5e', '13800000001', 'Sports fan and occasional ticket reseller. Based in Manchester.', 'fully_verified'),
(2, 'bob_chen', 'bob@example.com',    '$2a$10$3AaYtixXrpq9af4HA/GtTeXRPPmVVJd3Ah4O6KgAanXvKNExaAW5e', '13800000002', 'Concert lover. I buy and sell music event tickets regularly.', 'email_verified'),
(3, 'carla_m',  'carla@example.com',  '$2a$10$3AaYtixXrpq9af4HA/GtTeXRPPmVVJd3Ah4O6KgAanXvKNExaAW5e', '13800000003', 'Frequent flyer and business travel ticket broker.', 'fully_verified'),
(4, 'dawit_k',  'dawit@example.com',  '$2a$10$3AaYtixXrpq9af4HA/GtTeXRPPmVVJd3Ah4O6KgAanXvKNExaAW5e', '13800000004', 'NBA enthusiast. Based in LA.', 'phone_verified'),
(5, 'elena_p',  'elena@example.com',  '$2a$10$3AaYtixXrpq9af4HA/GtTeXRPPmVVJd3Ah4O6KgAanXvKNExaAW5e', '13800000005', 'Comedy night regular. Always have spare tickets.', 'unverified');

-- category (5 rows, including one self-referencing subcategory)
INSERT INTO category (id, name, parent_id) VALUES
(1, 'Sports', NULL),
(2, 'Music',  NULL),
(3, 'Flights', NULL),
(4, 'Other',  NULL),
(5, 'Football', 1);

-- ticket (5 rows)
INSERT INTO ticket (id, seller_id, category_id, event_name, event_date, event_location, seat_info, description, tags, start_price, status) VALUES
(1, 1, 5, 'Premier League Derby Match',       '2026-08-15 19:00:00', 'Old Trafford, Manchester',       'Block 12, Row F, Seat 23',  'Single seat, away end, transferable e-ticket.',                               'football,sports,manchester',  200.00, 'sold'),
(2, 2, 2, 'City Arena Summer Concert',        '2026-08-20 20:00:00', 'O2 Arena, London',               'Floor Standing - Section A','Mobile ticket transfer, valid ID required at entry.',                          'concert,music,london',        150.00, 'sold'),
(3, 3, 3, 'CDG-PVG Business Class Transfer',  '2026-09-01 09:30:00', 'Paris Charles de Gaulle Airport','Seat 4A',                   'Name change fee not included, buyer arranges separately with airline.',        'flight,business,paris',       500.00, 'sold'),
(4, 4, 1, 'NBA Finals Game 5',                '2026-09-10 19:30:00', 'Crypto.com Arena, Los Angeles',  'Lower Bowl, Section 112',   'Physical ticket, will be shipped via courier.',                                'basketball,nba,sports',       300.00, 'sold'),
(5, 5, 4, 'Downtown Comedy Night Standup',    '2026-07-25 21:00:00', 'The Comedy Cellar, New York',    'General Admission',         'Digital QR code ticket.',                                                      'comedy,standup,newyork',       80.00, 'sold'),
(6, 2, 2, 'Neon Skyline Live',                '2026-10-18 20:00:00', 'Mercedes-Benz Arena, Shanghai', 'Section B, Row 8',          'Mobile ticket transfer within 10 minutes of purchase.',                       'concert,music,shanghai',       320.00, 'active'),
(7, 1, 5, 'Manchester United vs Arsenal',     '2026-10-25 15:00:00', 'Old Trafford, Manchester',       'Block 18, Row D, Seat 11',  'Verified e-ticket with unobstructed view.',                                   'football,sports,manchester',  280.00, 'active'),
(8, 4, 1, 'Lakers vs Warriors',               '2026-11-07 19:30:00', 'Crypto.com Arena, Los Angeles',  'Lower Bowl, Section 108',   'Digital ticket delivered after payment confirmation.',                         'basketball,nba,los-angeles',  410.00, 'active'),
(9, 5, 4, 'Laugh Factory Saturday Showcase',  '2026-09-26 21:00:00', 'Laugh Factory, Los Angeles',     'General Admission',         'QR code ticket. Doors open at 8:00 PM.',                                      'comedy,standup,los-angeles',   75.00, 'active');

INSERT INTO ticket (id, seller_id, category_id, event_name, event_date, event_location, seat_info, description, tags, start_price, status) VALUES
(10, 1, 1, 'Manchester City vs Chelsea', '2026-10-31 15:00:00', 'Etihad Stadium, Manchester', 'East Stand, Row G', 'Verified mobile ticket with clear pitch view.', 'football,sports,manchester', 240.00, 'active'),
(11, 2, 2, 'The Weeknd After Hours Live', '2026-10-28 20:00:00', 'Rod Laver Arena, Melbourne', 'Lower Bowl, Row D', 'Mobile transfer available after payment.', 'concert,music,melbourne', 190.00, 'active'),
(12, 3, 3, 'LHR-JFK Premium Economy', '2026-10-22 08:10:00', 'Heathrow Airport, London', 'Seat 14C', 'Flexible travel date, name change available.', 'flight,travel,london', 430.00, 'active'),
(13, 4, 1, 'Warriors vs Celtics', '2026-12-02 19:30:00', 'Chase Center, San Francisco', 'Club Level, Section 4', 'Digital ticket with club access.', 'basketball,nba,san-francisco', 520.00, 'active'),
(14, 5, 4, 'Broadway Comedy Gala', '2026-11-15 20:00:00', 'Beacon Theatre, New York', 'Orchestra, Row H', 'Two seats together, instant delivery.', 'comedy,theater,new-york', 135.00, 'active'),
(15, 3, 3, 'Tokyo to Singapore Business Class', '2026-08-22 10:00:00', 'Haneda Airport, Tokyo', 'Seat 3A', 'Completed transfer with receipt included.', 'flight,travel,tokyo', 680.00, 'sold'),
(16, 4, 1, 'US Open Semi-Final', '2026-08-24 18:00:00', 'Arthur Ashe Stadium, New York', 'Section 118, Row B', 'Official digital ticket, excellent view.', 'tennis,sports,new-york', 390.00, 'sold'),
(17, 5, 4, 'Edinburgh Fringe Closing Night', '2026-08-26 19:00:00', 'Edinburgh Playhouse, Edinburgh', 'Stalls, Row C', 'Fast mobile transfer, one ticket.', 'comedy,theater,edinburgh', 110.00, 'sold');

INSERT INTO ticket_image (ticket_id, image_url, sort_order) VALUES

(1, '/uploads/tickets/1_seat_view.jpg', 0),
(1, '/uploads/tickets/1_stadium.jpg', 1),

(2, '/uploads/tickets/2_stage_view.jpg', 0),
(2, '/uploads/tickets/2_ticket_scan.jpg', 1),

(3, '/uploads/tickets/3_boarding_pass.jpg', 0),

(4, '/uploads/tickets/4_court_view.jpg', 0),

(5, '/uploads/tickets/5_venue_front.jpg', 0),

(6, '/uploads/tickets/6_neon_skyline.jpg', 0),

(7, '/uploads/tickets/7_old_trafford.jpg', 0),

(8, '/uploads/tickets/8_crypto_arena.jpg', 0),

(9, '/uploads/tickets/9_laugh_factory.jpg', 0),

(10, '/uploads/tickets/10_festival.jpg', 0),

(11, '/uploads/tickets/11_neon_skyline.jpg', 0),

(12, '/uploads/tickets/12_old_trafford.jpg', 0),

(13, '/uploads/tickets/13_crypto_arena.jpg', 0),

(14, '/uploads/tickets/14_laugh_factory.jpg', 0),

(15, '/uploads/tickets/15_festival.jpg', 0),

(16, '/uploads/tickets/16_us_open.jpg', 0),

(17, '/uploads/tickets/17_festival.jpg', 0);

-- auction (5 rows, all ended for test data consistency)
INSERT INTO auction (id, ticket_id, start_time, end_time, current_bid, winner_id, status) VALUES
(1, 1, '2026-08-01 10:00:00', '2026-08-08 10:00:00', 250.00, 2, 'ended'),
(2, 2, '2026-08-02 10:00:00', '2026-08-09 10:00:00', 180.00, 3, 'ended'),
(3, 3, '2026-08-03 10:00:00', '2026-08-10 10:00:00', 550.00, 4, 'ended'),
(4, 4, '2026-08-04 10:00:00', '2026-08-11 10:00:00', 320.00, 5, 'ended'),
(5, 5, '2026-08-05 10:00:00', '2026-08-12 10:00:00',  95.00, 1, 'ended'),
(6, 6, '2026-09-01 10:00:00', '2026-10-15 20:00:00', 350.00, NULL, 'ongoing'),
(7, 7, '2026-09-02 10:00:00', '2026-10-20 15:00:00', 295.00, NULL, 'ongoing'),
(8, 8, '2026-09-03 10:00:00', '2026-11-01 19:30:00', 450.00, NULL, 'ongoing'),
(9, 9, '2026-09-04 10:00:00', '2026-09-24 21:00:00',  90.00, NULL, 'ongoing');

INSERT INTO auction (id, ticket_id, start_time, end_time, current_bid, winner_id, status) VALUES
(10, 10, '2026-09-05 10:00:00', '2026-10-25 15:00:00', 265.00, 2, 'ongoing'),
(11, 11, '2026-09-05 10:30:00', '2026-10-20 20:00:00', 215.00, 5, 'ongoing'),
(12, 12, '2026-09-05 11:00:00', '2026-10-18 08:10:00', 470.00, 1, 'ongoing'),
(13, 13, '2026-09-05 11:30:00', '2026-11-20 19:30:00', 575.00, 2, 'ongoing'),
(14, 14, '2026-09-05 12:00:00', '2026-11-05 20:00:00', 155.00, 3, 'ongoing'),
(15, 15, '2026-08-15 10:00:00', '2026-08-20 10:00:00', 740.00, 1, 'ended'),
(16, 16, '2026-08-16 10:00:00', '2026-08-22 10:00:00', 450.00, 2, 'ended'),
(17, 17, '2026-08-17 10:00:00', '2026-08-24 10:00:00', 145.00, 4, 'ended');

-- bid (10 rows, 2 per auction)
INSERT INTO bid (auction_id, bidder_id, amount, bid_time) VALUES
(1, 3, 210.00, '2026-08-06 12:00:00'),
(1, 2, 250.00, '2026-08-08 09:45:00'),
(2, 4, 160.00, '2026-08-07 14:30:00'),
(2, 3, 180.00, '2026-08-09 09:50:00'),
(3, 2, 520.00, '2026-08-08 16:00:00'),
(3, 4, 550.00, '2026-08-10 09:55:00'),
(4, 1, 310.00, '2026-08-09 11:20:00'),
(4, 5, 320.00, '2026-08-11 09:40:00'),
(5, 3,  85.00, '2026-08-10 18:10:00'),
(5, 1,  95.00, '2026-08-12 09:30:00');

INSERT INTO bid (auction_id, bidder_id, amount, bid_time) VALUES
(10, 5, 250.00, '2026-09-06 12:10:00'), (10, 2, 265.00, '2026-09-07 09:20:00'),
(11, 1, 200.00, '2026-09-06 14:00:00'), (11, 5, 215.00, '2026-09-08 16:30:00'),
(12, 4, 450.00, '2026-09-07 11:05:00'), (12, 1, 470.00, '2026-09-09 10:15:00'),
(13, 5, 550.00, '2026-09-08 18:45:00'), (13, 2, 575.00, '2026-09-10 13:25:00'),
(14, 2, 145.00, '2026-09-09 17:20:00'), (14, 3, 155.00, '2026-09-11 08:40:00'),
(15, 2, 710.00, '2026-08-18 14:00:00'), (15, 1, 740.00, '2026-08-20 09:00:00'),
(16, 5, 420.00, '2026-08-19 15:30:00'), (16, 2, 450.00, '2026-08-22 09:15:00'),
(17, 1, 125.00, '2026-08-21 12:00:00'), (17, 4, 145.00, '2026-08-24 09:30:00');

-- orders (5 rows, one per ended auction)
INSERT INTO orders (id, auction_id, buyer_id, seller_id, final_price, status) VALUES
(1, 1, 2, 1, 250.00, 'completed'),
(2, 2, 3, 2, 180.00, 'completed'),
(3, 3, 4, 3, 550.00, 'confirmed'),
(4, 4, 5, 4, 320.00, 'disputed'),
(5, 5, 1, 5,  95.00, 'pending');

INSERT INTO orders (id, auction_id, buyer_id, seller_id, final_price, status) VALUES
(6, 15, 1, 3, 740.00, 'completed'),
(7, 16, 2, 4, 450.00, 'confirmed'),
(8, 17, 4, 5, 145.00, 'completed');

-- review (5 rows, buyer reviewing seller post-transaction)
INSERT INTO review (order_id, reviewer_id, reviewee_id, star_rating, comment) VALUES
(1, 2, 1, 5, 'Smooth transfer, exactly as described.'),
(2, 3, 2, 4, 'Good communication, ticket worked fine at the gate.'),
(3, 4, 3, 5, 'Seller was responsive about the airline name-change process.'),
(4, 5, 4, 2, 'Shipping took longer than promised, still waiting on resolution.'),
(5, 1, 5, 4, 'QR code worked, fast delivery.');

INSERT INTO review (order_id, reviewer_id, reviewee_id, star_rating, comment) VALUES
(6, 1, 3, 5, 'Excellent communication and a smooth flight ticket transfer.'),
(7, 2, 4, 4, 'Great seats and fast delivery.'),
(8, 4, 5, 5, 'Easy transaction and the comedy show was fantastic.');

-- trust_score (5 rows, one per user)
INSERT INTO trust_score (user_id, score, total_auctions, successful_auctions, disputes) VALUES
(1, 98.50, 2, 2, 0),
(2, 95.00, 2, 2, 0),
(3, 92.00, 2, 2, 0),
(4, 80.00, 2, 1, 1),
(5, 88.00, 2, 1, 1);

-- complaint (5 rows)
INSERT INTO complaint (user_id, order_id, description, ai_response, status) VALUES
(5, 4, 'Seller has not provided the shipping tracking number for the NBA ticket.', 'Please allow 48 hours for the seller to respond; if unresolved you can escalate to a dispute.', 'open'),
(2, NULL, 'How is the trust score calculated on this platform?', 'Trust score is based on completed auctions, dispute history, and peer star ratings.', 'resolved'),
(4, 3, 'Delay in receiving the e-ticket transfer confirmation after the auction ended.', 'The seller confirmed transfer was sent; please check your email spam folder.', 'resolved'),
(3, NULL, 'What is the refund policy if an auction gets cancelled by the seller?', 'Refunds for cancelled auctions are processed automatically within 3-5 business days.', 'resolved'),
(1, NULL, 'I noticed a duplicate listing for the same concert seat from another seller.', 'Thank you for the report, our team will review the listing for fraud.', 'open');

-- ai_log (5 rows)
INSERT INTO ai_log (user_id, feature_type, prompt_summary, response_summary) VALUES
(2, 'chatbot',       'User asked about platform refund policy',                    'Explained dispute resolution and refund timeline'),
(3, 'recommendation','Generated recommendations based on bidding history',          'Suggested 3 similar concert tickets in Music category'),
(4, 'price_advisor', 'Compared flight ticket price against historical auction data','Recommended buy now, price below historical average'),
(5, 'chatbot',       'Asked whether auctions auto-extend near expiry',              'Clarified that auctions do not auto-extend past end_time'),
(1, 'recommendation','Generated recommendations for sports category',               'Suggested 2 additional football listings');
