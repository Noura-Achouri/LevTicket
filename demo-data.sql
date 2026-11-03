-- Safe demo data refresh for an existing LevTicket database.
-- Seed account password: password123

UPDATE user
SET password_hash = '$2a$10$3AaYtixXrpq9af4HA/GtTeXRPPmVVJd3Ah4O6KgAanXvKNExaAW5e'
WHERE id IN (1, 2, 3, 4, 5);

INSERT IGNORE INTO ticket (id, seller_id, category_id, event_name, event_date, event_location, seat_info, description, tags, start_price, status) VALUES
(100, 1, 1, 'Manchester City vs Chelsea', '2026-10-31 15:00:00', 'Etihad Stadium, Manchester', 'East Stand, Row G', 'Verified mobile ticket with clear pitch view.', 'football,sports,manchester', 240.00, 'active'),
(101, 2, 2, 'The Weeknd After Hours Live', '2026-10-28 20:00:00', 'Rod Laver Arena, Melbourne', 'Lower Bowl, Row D', 'Mobile transfer available after payment.', 'concert,music,melbourne', 190.00, 'active'),
(102, 3, 3, 'LHR-JFK Premium Economy', '2026-10-22 08:10:00', 'Heathrow Airport, London', 'Seat 14C', 'Flexible travel date, name change available.', 'flight,travel,london', 430.00, 'active'),
(103, 4, 1, 'Warriors vs Celtics', '2026-12-02 19:30:00', 'Chase Center, San Francisco', 'Club Level, Section 4', 'Digital ticket with club access.', 'basketball,nba,san-francisco', 520.00, 'active'),
(104, 5, 4, 'Broadway Comedy Gala', '2026-11-15 20:00:00', 'Beacon Theatre, New York', 'Orchestra, Row H', 'Two seats together, instant delivery.', 'comedy,theater,new-york', 135.00, 'active'),
(105, 3, 3, 'Tokyo to Singapore Business Class', '2026-08-22 10:00:00', 'Haneda Airport, Tokyo', 'Seat 3A', 'Completed transfer with receipt included.', 'flight,travel,tokyo', 680.00, 'sold'),
(106, 4, 1, 'US Open Semi-Final', '2026-08-24 18:00:00', 'Arthur Ashe Stadium, New York', 'Section 118, Row B', 'Official digital ticket, excellent view.', 'tennis,sports,new-york', 390.00, 'sold'),
(107, 5, 4, 'Edinburgh Fringe Closing Night', '2026-08-26 19:00:00', 'Edinburgh Playhouse, Edinburgh', 'Stalls, Row C', 'Fast mobile transfer, one ticket.', 'comedy,theater,edinburgh', 110.00, 'sold');

INSERT IGNORE INTO auction (id, ticket_id, start_time, end_time, current_bid, winner_id, status) VALUES
(100, 100, '2026-09-05 10:00:00', '2026-10-25 15:00:00', 265.00, 2, 'ongoing'),
(101, 101, '2026-09-05 10:30:00', '2026-10-20 20:00:00', 215.00, 5, 'ongoing'),
(102, 102, '2026-09-05 11:00:00', '2026-10-18 08:10:00', 470.00, 1, 'ongoing'),
(103, 103, '2026-09-05 11:30:00', '2026-11-20 19:30:00', 575.00, 2, 'ongoing'),
(104, 104, '2026-09-05 12:00:00', '2026-11-05 20:00:00', 155.00, 3, 'ongoing'),
(105, 105, '2026-08-15 10:00:00', '2026-08-20 10:00:00', 740.00, 1, 'ended'),
(106, 106, '2026-08-16 10:00:00', '2026-08-22 10:00:00', 450.00, 2, 'ended'),
(107, 107, '2026-08-17 10:00:00', '2026-08-24 10:00:00', 145.00, 4, 'ended');

INSERT IGNORE INTO bid (auction_id, bidder_id, amount, bid_time) VALUES
(100, 5, 250.00, '2026-09-06 12:10:00'), (100, 2, 265.00, '2026-09-07 09:20:00'),
(101, 1, 200.00, '2026-09-06 14:00:00'), (101, 5, 215.00, '2026-09-08 16:30:00'),
(102, 4, 450.00, '2026-09-07 11:05:00'), (102, 1, 470.00, '2026-09-09 10:15:00'),
(103, 5, 550.00, '2026-09-08 18:45:00'), (103, 2, 575.00, '2026-09-10 13:25:00'),
(104, 2, 145.00, '2026-09-09 17:20:00'), (104, 3, 155.00, '2026-09-11 08:40:00'),
(105, 2, 710.00, '2026-08-18 14:00:00'), (105, 1, 740.00, '2026-08-20 09:00:00'),
(106, 5, 420.00, '2026-08-19 15:30:00'), (106, 2, 450.00, '2026-08-22 09:15:00'),
(107, 1, 125.00, '2026-08-21 12:00:00'), (107, 4, 145.00, '2026-08-24 09:30:00');

INSERT IGNORE INTO orders (id, auction_id, buyer_id, seller_id, final_price, status) VALUES
(100, 105, 1, 3, 740.00, 'completed'),
(101, 106, 2, 4, 450.00, 'confirmed'),
(102, 107, 4, 5, 145.00, 'completed');

INSERT IGNORE INTO review (order_id, reviewer_id, reviewee_id, star_rating, comment) VALUES
(100, 1, 3, 5, 'Excellent communication and a smooth flight ticket transfer.'),
(101, 2, 4, 4, 'Great seats and fast delivery.'),
(102, 4, 5, 5, 'Easy transaction and the comedy show was fantastic.');
