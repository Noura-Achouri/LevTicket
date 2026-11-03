-- ============================================
-- BidPass Demo Seed Data
-- Run AFTER init.sql (this adds to existing data)
-- Adds: 10 users, 45 tickets, 45 auctions,
--       90 bids, 15 orders, 10 reviews,
--       10 trust_score rows, 50 ticket images
-- ============================================

USE bidpass;

-- ============================================
-- EXTRA USERS (id 6-15)
-- ============================================
INSERT INTO user (id, username, email, password_hash, phone, bio, verification_status) VALUES
(6,  'james_k',   'james@example.com',  '$2a$10$hashFFFF', '13800000006', 'Sports fan from London. Sell tickets I can no longer use.', 'fully_verified'),
(7,  'sofia_l',   'sofia@example.com',  '$2a$10$hashGGGG', '13800000007', 'Music lover and concert regular. Based in Paris.', 'email_verified'),
(8,  'mike_t',    'mike@example.com',   '$2a$10$hashHHHH', '13800000008', 'Frequent flyer, business class ticket broker.', 'fully_verified'),
(9,  'yuki_n',    'yuki@example.com',   '$2a$10$hashIIII', '13800000009', 'NBA and football fan. Tokyo based.', 'phone_verified'),
(10, 'omar_h',    'omar@example.com',   '$2a$10$hashJJJJ', '13800000010', 'Event enthusiast. Always have spare tickets.', 'fully_verified'),
(11, 'priya_s',   'priya@example.com',  '$2a$10$hashKKKK', '13800000011', 'Music and arts lover from Mumbai.', 'email_verified'),
(12, 'lucas_b',   'lucas@example.com',  '$2a$10$hashLLLL', '13800000012', 'Sports tickets specialist. Brazil based.', 'fully_verified'),
(13, 'anna_w',    'anna@example.com',   '$2a$10$hashMMMM', '13800000013', 'Concert goer and travel enthusiast.', 'phone_verified'),
(14, 'chen_x',    'chen@example.com',   '$2a$10$hashNNNN', '13800000014', 'Ticket reseller with 5 years experience.', 'fully_verified'),
(15, 'fatima_z',  'fatima@example.com', '$2a$10$hashOOOO', '13800000015', 'Arts and culture event fan. Dubai based.', 'email_verified');

-- ============================================
-- EXTRA TICKETS (id 6-50)
-- Images reference files in /uploads/tickets/
-- Put sports.jpg, music.jpg, flight.jpg, other.jpg
-- inside backend/src/main/resources/static/uploads/tickets/
-- ============================================
INSERT INTO ticket (id, seller_id, category_id, event_name, event_date, event_location, seat_info, description, tags, start_price, status) VALUES
-- SPORTS (category 1 & 5)
(6,  6,  5, 'Champions League Final 2026',          '2026-11-01 20:45:00', 'Wembley Stadium, London',           'Block 114, Row 8, Seat 12',  'Original ticket, unused. Selling due to travel conflict.',          'football,champions-league,london',    350.00, 'active'),
(7,  7,  5, 'Premier League - Man City vs Arsenal', '2026-10-15 17:30:00', 'Etihad Stadium, Manchester',        'North Stand, Row F, Seat 22','Great view from the north end. E-ticket transfer.',                'football,premier-league,manchester',  180.00, 'active'),
(8,  8,  1, 'NBA Finals Game 7',                    '2026-10-20 20:00:00', 'Chase Center, San Francisco',       'Section 101, Row 5, Seat 9', 'Floor level seats. Physical ticket, shipped via courier.',          'basketball,nba,san-francisco',        500.00, 'active'),
(9,  9,  1, 'Wimbledon Men\'s Final 2026',           '2026-10-06 14:00:00', 'All England Club, London',          'Centre Court, Row 15',       'Centre court experience. Face value resale.',                      'tennis,wimbledon,london',             420.00, 'active'),
(10, 10, 1, 'Formula 1 British Grand Prix',         '2026-10-10 14:00:00', 'Silverstone Circuit, Northampton',  'General Admission - Becketts','Camping included. Full race weekend pass.',                        'f1,motorsport,silverstone',           290.00, 'active'),
(11, 6,  1, 'UFC 310 Championship Night',           '2026-10-25 18:00:00', 'T-Mobile Arena, Las Vegas',         'Lower Bowl, Section 5',      'Title fight night. Two seats available together.',                 'ufc,mma,las-vegas',                   380.00, 'active'),
(12, 7,  5, 'World Cup Qualifier - England vs Spain','2026-11-15 19:45:00', 'Wembley Stadium, London',           'Upper Tier, Row 3, Seat 4',  'Historic match. Original purchase from FA ballot.',                'football,world-cup,wembley',          220.00, 'active'),
(13, 8,  1, 'Australian Open Tennis Final',         '2026-10-30 19:30:00', 'Melbourne Park, Melbourne',         'Rod Laver Arena, Row 10',    'Night session final ticket. Digital transfer.',                    'tennis,grand-slam,melbourne',         310.00, 'active'),
(14, 9,  1, 'Golf Masters Tournament - Day 4',      '2026-11-08 09:00:00', 'Augusta National Golf Club',        'Badge Pass - Full Sunday',   'Sunday badge for the most iconic day in golf.',                    'golf,masters,augusta',                450.00, 'active'),
(15, 10, 1, 'Boxing World Title Fight - Fury vs Joshua','2026-11-22 20:00:00','Tottenham Hotspur Stadium, London','Floor Standing A',          'The biggest fight of 2026. Floor section.',                        'boxing,sports,london',                600.00, 'active'),
(16, 11, 5, 'La Liga - Real Madrid vs Barcelona',   '2026-10-18 21:00:00', 'Santiago Bernabeu, Madrid',         'Lower Tier, Sector 12',      'El Clasico tickets. Very rare. Name transfer required.',           'football,laliga,madrid',              480.00, 'active'),
(17, 12, 1, 'Olympics 2026 Opening Ceremony',       '2026-12-04 20:00:00', 'National Stadium, Brisbane',        'Category B - East Stand',    'Once in a lifetime experience. Official resale.',                  'olympics,ceremony,brisbane',          350.00, 'active'),
(18, 13, 1, 'Super Bowl LX 2026',                   '2026-12-15 18:30:00', 'SoFi Stadium, Los Angeles',         'Upper Level, Section 322',   'Super Bowl experience. Hotel package not included.',               'american-football,super-bowl,la',     800.00, 'active'),
(19, 14, 1, 'Cricket World Cup Final',              '2026-11-29 10:00:00', 'Lord\'s Cricket Ground, London',    'Grandstand, Row D, Seat 18', 'World Cup final at Lord\'s. Digital ticket.',                      'cricket,world-cup,lords',             260.00, 'active'),
(20, 15, 5, 'Serie A - Inter Milan vs Juventus',    '2026-10-12 20:45:00', 'San Siro, Milan',                   'Curva Nord, Row 22',         'Derby d\'Italia. E-ticket, Italian ID required at gate.',          'football,serie-a,milan',              190.00, 'active'),

-- MUSIC (category 2)
(21, 6,  2, 'Taylor Swift The Eras Tour - London',  '2026-10-03 19:00:00', 'Wembley Stadium, London',           'Floor Standing - Pit Section','Pit section ticket. Mobile transfer only.',                        'taylor-swift,pop,concert',            320.00, 'active'),
(22, 7,  2, 'Coldplay Music of the Spheres Tour',   '2026-10-08 20:00:00', 'Olympic Stadium, Berlin',           'Cat A - Block 12',           'Full LED wristband experience. One of the best shows.',            'coldplay,pop,berlin',                 210.00, 'active'),
(23, 8,  2, 'BTS World Tour 2026',                  '2026-11-05 18:00:00', 'Olympic Stadium, Seoul',            'Floor Section B',            'BTS comeback world tour. Floor section B.',                        'bts,kpop,seoul',                      280.00, 'active'),
(24, 9,  2, 'Ed Sheeran Mathematics Tour',          '2026-10-22 20:00:00', 'Aviva Stadium, Dublin',             'General Admission Floor',    'Ed Sheeran solo show. GA floor ticket.',                           'ed-sheeran,pop,dublin',               150.00, 'active'),
(25, 10, 2, 'Beyonce Renaissance World Tour',       '2026-11-12 20:30:00', 'MetLife Stadium, New York',         'Lower Level, Section 138',   'Beyonce Renaissance Tour NYC date. Sold out everywhere.',          'beyonce,rnb,new-york',                450.00, 'active'),
(26, 11, 2, 'Drake It\'s All A Blur Tour',          '2026-10-28 21:00:00', 'Scotiabank Arena, Toronto',         'Floor Section B2',           'Drake hometown show. Floor tickets.',                              'drake,hiphop,toronto',                300.00, 'active'),
(27, 12, 2, 'Adele Las Vegas Residency',            '2026-11-02 20:00:00', 'Colosseum at Caesars Palace, LV',  'Stall Row G, Seat 14',       'Adele Vegas residency. Intimate venue. Rare resale.',              'adele,pop,las-vegas',                 700.00, 'active'),
(28, 13, 2, 'The Weeknd After Hours Tour 2026',     '2026-10-16 21:00:00', 'Rogers Centre, Toronto',            'Lower Bowl, Section 115',    'The Weeknd world tour. Lower bowl seats.',                         'the-weeknd,rnb,toronto',              220.00, 'active'),
(29, 14, 2, 'Harry Styles Love on Tour',            '2026-11-20 19:30:00', 'Madison Square Garden, New York',   'Floor Level - GA',           'Harry Styles MSG show. GA floor standing.',                        'harry-styles,pop,new-york',           380.00, 'active'),
(30, 15, 2, 'Billie Eilish Happier Than Ever Tour', '2026-10-14 20:00:00', 'The O2 Arena, London',              'Block 112, Row P, Seat 3',   'Billie Eilish London show. Seated ticket.',                        'billie-eilish,pop,london',            175.00, 'active'),
(31, 6,  2, 'Post Malone Twelve Carat Toothache Tour','2026-11-10 21:00:00','United Center, Chicago',           'Lower Level, Section 102',   'Post Malone Chicago date. Sold out on primary.',                   'post-malone,hiphop,chicago',          200.00, 'active'),
(32, 7,  2, 'Dua Lipa Future Nostalgia Tour',       '2026-10-26 20:00:00', 'Ziggo Dome, Amsterdam',             'Floor Section A',            'Dua Lipa Amsterdam show. Floor section.',                          'dua-lipa,pop,amsterdam',              190.00, 'active'),
(33, 8,  2, 'Eminem The Death of Slim Shady Tour',  '2026-11-18 20:30:00', 'Ford Field, Detroit',               'Lower Bowl, Section 118',    'Eminem home city show. Lower bowl.',                               'eminem,hiphop,detroit',               260.00, 'active'),
(34, 9,  2, 'Bruno Mars World Tour 2026',           '2026-10-31 20:00:00', 'Sphere, Las Vegas',                 'Section 201, Row C',         'Bruno Mars at the Sphere. Incredible visual experience.',          'bruno-mars,pop,las-vegas',            550.00, 'active'),
(35, 10, 2, 'Kendrick Lamar Grand National Tour',   '2026-11-25 21:00:00', 'Kia Forum, Los Angeles',            'Floor - Section B',          'Kendrick LA hometown show. Floor section B.',                      'kendrick-lamar,hiphop,los-angeles',   310.00, 'active'),

-- FLIGHTS (category 3)
(36, 11, 3, 'London Heathrow to JFK Business Class','2026-10-05 10:30:00', 'London Heathrow Airport (LHR)',     'Seat 4A - Business Class',   'British Airways business class. Name change fee not included.',    'flight,business,london-new-york',     650.00, 'active'),
(37, 12, 3, 'Dubai to Tokyo First Class',           '2026-10-09 02:15:00', 'Dubai International Airport (DXB)', 'Suite 1K - First Class',     'Emirates first class suite. Private mini-bar included.',           'flight,first-class,dubai-tokyo',      1200.00,'active'),
(38, 13, 3, 'Paris CDG to Beijing Business',        '2026-10-13 11:45:00', 'Paris Charles de Gaulle Airport',  'Seat 7C - Business Class',   'Air France business class. Lie-flat seats.',                      'flight,business,paris-beijing',        580.00, 'active'),
(39, 14, 3, 'Singapore to LA Business Class',       '2026-10-17 23:50:00', 'Changi Airport, Singapore (SIN)',  'Seat 11A - Business Class',  'Singapore Airlines business. World\'s best airline seat.',         'flight,business,singapore-la',        890.00, 'active'),
(40, 15, 3, 'Hong Kong to London First Class',      '2026-10-21 11:30:00', 'Hong Kong International Airport',  'Suite 2A - First Class',     'Cathay Pacific first class suite. Champagne and pajamas.',         'flight,first-class,hong-kong-london', 1450.00,'active'),
(41, 6,  3, 'New York JFK to Tokyo Premium Economy','2026-10-24 13:00:00', 'John F. Kennedy Airport, New York', 'Seat 23A - Premium Economy', 'JAL premium economy. Extra legroom and meal included.',            'flight,premium-economy,ny-tokyo',     380.00, 'active'),
(42, 7,  3, 'Amsterdam to Singapore Business',      '2026-10-27 21:10:00', 'Amsterdam Schiphol Airport (AMS)', 'Seat 9B - Business Class',   'KLM World Business Class. Direct flight 12 hours.',               'flight,business,amsterdam-singapore', 720.00, 'active'),
(43, 8,  3, 'Sydney to Paris Business Class',       '2026-11-01 16:30:00', 'Sydney Kingsford Smith Airport',   'Seat 6A - Business Class',   'Qantas business class. Stopover in Singapore.',                    'flight,business,sydney-paris',        980.00, 'active'),
(44, 9,  3, 'Seoul Incheon to London Premium Eco',  '2026-11-05 13:45:00', 'Incheon International Airport',    'Seat 31A - Premium Economy', 'Korean Air premium economy. Bubble wrap seat protection.',         'flight,premium-economy,seoul-london', 450.00, 'active'),
(45, 10, 3, 'Shanghai to New York Business Class',  '2026-11-09 15:00:00', 'Shanghai Pudong International',    'Seat 8C - Business Class',   'Air China business. Direct 14 hour flight.',                       'flight,business,shanghai-new-york',   820.00, 'active'),

-- OTHER (category 4)
(46, 11, 4, 'Downtown Comedy Night Special',        '2026-10-07 21:00:00', 'The Comedy Store, Los Angeles',     'VIP Booth - Table 4',        'Comedy night with 5 headline acts. VIP booth includes drinks.',    'comedy,vip,los-angeles',              120.00, 'active'),
(47, 12, 4, 'Hamilton Broadway Show',               '2026-10-11 19:30:00', 'Richard Rodgers Theatre, New York', 'Orchestra Row H, Seat 101',  'Hamilton original Broadway cast. Very hard to get.',               'broadway,theatre,new-york',           280.00, 'active'),
(48, 13, 4, 'Cannes Film Festival Opening Night',   '2026-10-19 20:00:00', 'Palais des Festivals, Cannes',      'Grand Auditorium - Row D',   'Red carpet opening night. Black tie event.',                       'film-festival,cinema,cannes',         350.00, 'active'),
(49, 14, 4, 'TEDx London Conference 2026',          '2026-10-23 09:00:00', 'Royal Festival Hall, London',       'General Admission',          'Full day TED conference. Networking lunch included.',              'ted,conference,london',               180.00, 'active'),
(50, 15, 4, 'Paris Fashion Week Front Row',         '2026-10-29 15:00:00', 'Grand Palais, Paris',               'Front Row - Chanel Show',    'Front row at the Chanel autumn show. Extremely rare.',             'fashion,paris,front-row',             950.00, 'active');

-- ============================================
-- TICKET IMAGES (one or two per ticket)
-- Uses 4 shared images repeated across categories
-- Drop sports.jpg, music.jpg, flight.jpg, other.jpg
-- into: backend/src/main/resources/static/uploads/tickets/
-- ============================================
INSERT INTO ticket_image (ticket_id, image_url, sort_order) VALUES
(6,  '/uploads/tickets/sports.jpg', 0),
(7,  '/uploads/tickets/sports.jpg', 0),
(8,  '/uploads/tickets/sports.jpg', 0),
(9,  '/uploads/tickets/sports.jpg', 0),
(10, '/uploads/tickets/sports.jpg', 0),
(11, '/uploads/tickets/sports.jpg', 0),
(12, '/uploads/tickets/sports.jpg', 0),
(13, '/uploads/tickets/sports.jpg', 0),
(14, '/uploads/tickets/sports.jpg', 0),
(15, '/uploads/tickets/sports.jpg', 0),
(16, '/uploads/tickets/sports.jpg', 0),
(17, '/uploads/tickets/sports.jpg', 0),
(18, '/uploads/tickets/sports.jpg', 0),
(19, '/uploads/tickets/sports.jpg', 0),
(20, '/uploads/tickets/sports.jpg', 0),
(21, '/uploads/tickets/music.jpg',  0),
(22, '/uploads/tickets/music.jpg',  0),
(23, '/uploads/tickets/music.jpg',  0),
(24, '/uploads/tickets/music.jpg',  0),
(25, '/uploads/tickets/music.jpg',  0),
(26, '/uploads/tickets/music.jpg',  0),
(27, '/uploads/tickets/music.jpg',  0),
(28, '/uploads/tickets/music.jpg',  0),
(29, '/uploads/tickets/music.jpg',  0),
(30, '/uploads/tickets/music.jpg',  0),
(31, '/uploads/tickets/music.jpg',  0),
(32, '/uploads/tickets/music.jpg',  0),
(33, '/uploads/tickets/music.jpg',  0),
(34, '/uploads/tickets/music.jpg',  0),
(35, '/uploads/tickets/music.jpg',  0),
(36, '/uploads/tickets/flight.jpg', 0),
(37, '/uploads/tickets/flight.jpg', 0),
(38, '/uploads/tickets/flight.jpg', 0),
(39, '/uploads/tickets/flight.jpg', 0),
(40, '/uploads/tickets/flight.jpg', 0),
(41, '/uploads/tickets/flight.jpg', 0),
(42, '/uploads/tickets/flight.jpg', 0),
(43, '/uploads/tickets/flight.jpg', 0),
(44, '/uploads/tickets/flight.jpg', 0),
(45, '/uploads/tickets/flight.jpg', 0),
(46, '/uploads/tickets/other.jpg',  0),
(47, '/uploads/tickets/other.jpg',  0),
(48, '/uploads/tickets/other.jpg',  0),
(49, '/uploads/tickets/other.jpg',  0),
(50, '/uploads/tickets/other.jpg',  0);

-- ============================================
-- AUCTIONS (id 6-50)
-- Mix of ongoing (future end_time) and ended
-- Current date: September 9, 2026
-- Ongoing: end_time in future
-- Ended: end_time in past
-- ============================================
INSERT INTO auction (id, ticket_id, start_time, end_time, current_bid, winner_id, status) VALUES
-- ONGOING auctions (30 tickets still active)
(6,  6,  '2026-09-05 10:00:00', '2026-09-15 20:00:00', 420.00,  NULL, 'ongoing'),
(7,  7,  '2026-09-06 10:00:00', '2026-09-16 10:00:00', 210.00,  NULL, 'ongoing'),
(8,  8,  '2026-09-07 10:00:00', '2026-09-17 10:00:00', 580.00,  NULL, 'ongoing'),
(9,  9,  '2026-09-05 10:00:00', '2026-09-14 10:00:00', 490.00,  NULL, 'ongoing'),
(10, 10, '2026-09-06 10:00:00', '2026-09-13 10:00:00', 330.00,  NULL, 'ongoing'),
(11, 11, '2026-09-07 10:00:00', '2026-09-18 10:00:00', 410.00,  NULL, 'ongoing'),
(12, 12, '2026-09-05 10:00:00', '2026-09-20 10:00:00', 260.00,  NULL, 'ongoing'),
(13, 13, '2026-09-06 10:00:00', '2026-09-19 10:00:00', 350.00,  NULL, 'ongoing'),
(14, 14, '2026-09-07 10:00:00', '2026-09-21 10:00:00', 510.00,  NULL, 'ongoing'),
(15, 15, '2026-09-05 10:00:00', '2026-09-22 10:00:00', 680.00,  NULL, 'ongoing'),
(16, 16, '2026-09-06 10:00:00', '2026-09-23 10:00:00', 550.00,  NULL, 'ongoing'),
(17, 17, '2026-09-07 10:00:00', '2026-09-24 10:00:00', 400.00,  NULL, 'ongoing'),
(18, 18, '2026-09-05 10:00:00', '2026-09-25 10:00:00', 900.00,  NULL, 'ongoing'),
(19, 19, '2026-09-06 10:00:00', '2026-09-26 10:00:00', 300.00,  NULL, 'ongoing'),
(20, 20, '2026-09-07 10:00:00', '2026-09-27 10:00:00', 230.00,  NULL, 'ongoing'),
(21, 21, '2026-09-05 10:00:00', '2026-09-28 10:00:00', 380.00,  NULL, 'ongoing'),
(22, 22, '2026-09-06 10:00:00', '2026-09-29 10:00:00', 250.00,  NULL, 'ongoing'),
(23, 23, '2026-09-07 10:00:00', '2026-09-30 10:00:00', 320.00,  NULL, 'ongoing'),
(24, 24, '2026-09-05 10:00:00', '2026-09-15 10:00:00', 175.00,  NULL, 'ongoing'),
(25, 25, '2026-09-06 10:00:00', '2026-09-16 10:00:00', 520.00,  NULL, 'ongoing'),
(26, 26, '2026-09-07 10:00:00', '2026-09-17 10:00:00', 360.00,  NULL, 'ongoing'),
(27, 27, '2026-09-05 10:00:00', '2026-09-18 10:00:00', 780.00,  NULL, 'ongoing'),
(28, 28, '2026-09-06 10:00:00', '2026-09-19 10:00:00', 260.00,  NULL, 'ongoing'),
(29, 29, '2026-09-07 10:00:00', '2026-09-20 10:00:00', 430.00,  NULL, 'ongoing'),
(30, 30, '2026-09-05 10:00:00', '2026-09-21 10:00:00', 210.00,  NULL, 'ongoing'),
(31, 31, '2026-09-06 10:00:00', '2026-09-22 10:00:00', 240.00,  NULL, 'ongoing'),
(32, 32, '2026-09-07 10:00:00', '2026-09-23 10:00:00', 225.00,  NULL, 'ongoing'),
(33, 33, '2026-09-05 10:00:00', '2026-09-24 10:00:00', 300.00,  NULL, 'ongoing'),
(34, 34, '2026-09-06 10:00:00', '2026-09-25 10:00:00', 620.00,  NULL, 'ongoing'),
(35, 35, '2026-09-07 10:00:00', '2026-09-26 10:00:00', 360.00,  NULL, 'ongoing'),
-- ENDED auctions (15 tickets sold)
(36, 36, '2026-08-20 10:00:00', '2026-09-01 10:00:00', 720.00,  3,  'ended'),
(37, 37, '2026-08-21 10:00:00', '2026-09-02 10:00:00', 1350.00, 4,  'ended'),
(38, 38, '2026-08-22 10:00:00', '2026-09-03 10:00:00', 650.00,  5,  'ended'),
(39, 39, '2026-08-23 10:00:00', '2026-09-04 10:00:00', 950.00,  6,  'ended'),
(40, 40, '2026-08-24 10:00:00', '2026-09-05 10:00:00', 1580.00, 7,  'ended'),
(41, 41, '2026-08-25 10:00:00', '2026-09-06 10:00:00', 420.00,  8,  'ended'),
(42, 42, '2026-08-26 10:00:00', '2026-09-07 10:00:00', 800.00,  9,  'ended'),
(43, 43, '2026-08-27 10:00:00', '2026-09-08 10:00:00', 1100.00, 10, 'ended'),
(44, 44, '2026-08-28 10:00:00', '2026-09-01 10:00:00', 500.00,  11, 'ended'),
(45, 45, '2026-08-29 10:00:00', '2026-09-02 10:00:00', 900.00,  12, 'ended'),
(46, 46, '2026-08-30 10:00:00', '2026-09-03 10:00:00', 140.00,  13, 'ended'),
(47, 47, '2026-08-31 10:00:00', '2026-09-04 10:00:00', 320.00,  14, 'ended'),
(48, 48, '2026-09-01 10:00:00', '2026-09-05 10:00:00', 390.00,  15, 'ended'),
(49, 49, '2026-09-02 10:00:00', '2026-09-06 10:00:00', 210.00,  1,  'ended'),
(50, 50, '2026-09-03 10:00:00', '2026-09-07 10:00:00', 1050.00, 2,  'ended');

-- ============================================
-- BIDS (2-3 per auction, ongoing and ended)
-- ============================================
INSERT INTO bid (auction_id, bidder_id, amount, bid_time) VALUES
-- ongoing auctions - showing bidding activity
(6,  2,  375.00, '2026-09-06 14:00:00'), (6,  9,  420.00, '2026-09-08 10:30:00'),
(7,  3,  195.00, '2026-09-07 11:00:00'), (7,  10, 210.00, '2026-09-08 16:00:00'),
(8,  4,  540.00, '2026-09-07 13:00:00'), (8,  11, 580.00, '2026-09-09 09:00:00'),
(9,  5,  455.00, '2026-09-06 15:00:00'), (9,  12, 490.00, '2026-09-08 11:00:00'),
(10, 6,  305.00, '2026-09-07 12:00:00'), (10, 13, 330.00, '2026-09-08 14:00:00'),
(11, 7,  385.00, '2026-09-07 16:00:00'), (11, 14, 410.00, '2026-09-09 08:00:00'),
(12, 8,  240.00, '2026-09-06 17:00:00'), (12, 15, 260.00, '2026-09-08 12:00:00'),
(13, 9,  325.00, '2026-09-07 14:00:00'), (13, 1,  350.00, '2026-09-09 10:00:00'),
(14, 10, 475.00, '2026-09-07 15:00:00'), (14, 2,  510.00, '2026-09-09 11:00:00'),
(15, 11, 640.00, '2026-09-06 16:00:00'), (15, 3,  680.00, '2026-09-08 13:00:00'),
(16, 12, 520.00, '2026-09-07 17:00:00'), (16, 4,  550.00, '2026-09-09 09:30:00'),
(17, 13, 375.00, '2026-09-06 18:00:00'), (17, 5,  400.00, '2026-09-08 15:00:00'),
(18, 14, 855.00, '2026-09-07 19:00:00'), (18, 6,  900.00, '2026-09-09 10:30:00'),
(19, 15, 280.00, '2026-09-06 14:30:00'), (19, 7,  300.00, '2026-09-08 16:30:00'),
(20, 1,  215.00, '2026-09-07 11:30:00'), (20, 8,  230.00, '2026-09-09 11:30:00'),
(21, 2,  355.00, '2026-09-06 15:30:00'), (21, 9,  380.00, '2026-09-08 17:00:00'),
(22, 3,  230.00, '2026-09-07 12:30:00'), (22, 10, 250.00, '2026-09-09 12:00:00'),
(23, 4,  300.00, '2026-09-07 16:30:00'), (23, 11, 320.00, '2026-09-08 18:00:00'),
(24, 5,  160.00, '2026-09-06 13:30:00'), (24, 12, 175.00, '2026-09-08 14:30:00'),
(25, 6,  480.00, '2026-09-07 17:30:00'), (25, 13, 520.00, '2026-09-09 13:00:00'),
(26, 7,  335.00, '2026-09-06 18:30:00'), (26, 14, 360.00, '2026-09-08 19:00:00'),
(27, 8,  740.00, '2026-09-07 14:30:00'), (27, 15, 780.00, '2026-09-09 14:00:00'),
(28, 9,  240.00, '2026-09-06 15:30:00'), (28, 1,  260.00, '2026-09-08 15:30:00'),
(29, 10, 400.00, '2026-09-07 16:30:00'), (29, 2,  430.00, '2026-09-09 15:00:00'),
(30, 11, 195.00, '2026-09-06 17:30:00'), (30, 3,  210.00, '2026-09-08 16:00:00'),
-- ended auctions - the winning bids
(36, 2,  680.00, '2026-08-25 10:00:00'), (36, 3,  720.00, '2026-09-01 09:45:00'),
(37, 4,  1280.00,'2026-08-27 11:00:00'), (37, 4,  1350.00,'2026-09-02 09:50:00'),
(38, 5,  610.00, '2026-08-28 12:00:00'), (38, 5,  650.00, '2026-09-03 09:55:00'),
(39, 6,  900.00, '2026-08-29 13:00:00'), (39, 6,  950.00, '2026-09-04 09:40:00'),
(40, 7,  1490.00,'2026-08-30 14:00:00'), (40, 7,  1580.00,'2026-09-05 09:45:00'),
(41, 8,  395.00, '2026-08-31 15:00:00'), (41, 8,  420.00, '2026-09-06 09:50:00'),
(42, 9,  760.00, '2026-09-01 16:00:00'), (42, 9,  800.00, '2026-09-07 09:55:00'),
(43, 10, 1050.00,'2026-09-02 17:00:00'), (43, 10, 1100.00,'2026-09-08 09:40:00'),
(44, 11, 470.00, '2026-08-30 18:00:00'), (44, 11, 500.00, '2026-09-01 09:45:00'),
(45, 12, 850.00, '2026-08-31 19:00:00'), (45, 12, 900.00, '2026-09-02 09:50:00'),
(46, 13, 130.00, '2026-09-01 10:00:00'), (46, 13, 140.00, '2026-09-03 09:55:00'),
(47, 14, 300.00, '2026-09-02 11:00:00'), (47, 14, 320.00, '2026-09-04 09:40:00'),
(48, 15, 365.00, '2026-09-03 12:00:00'), (48, 15, 390.00, '2026-09-05 09:45:00'),
(49, 1,  195.00, '2026-09-04 13:00:00'), (49, 1,  210.00, '2026-09-06 09:50:00'),
(50, 2,  980.00, '2026-09-05 14:00:00'), (50, 2,  1050.00,'2026-09-07 09:55:00');

-- ============================================
-- ORDERS (for ended auctions 36-50)
-- ============================================
INSERT INTO orders (auction_id, buyer_id, seller_id, final_price, status) VALUES
(36, 3,  11, 720.00,  'completed'),
(37, 4,  12, 1350.00, 'completed'),
(38, 5,  13, 650.00,  'completed'),
(39, 6,  14, 950.00,  'completed'),
(40, 7,  15, 1580.00, 'completed'),
(41, 8,  6,  420.00,  'confirmed'),
(42, 9,  7,  800.00,  'completed'),
(43, 10, 8,  1100.00, 'completed'),
(44, 11, 9,  500.00,  'confirmed'),
(45, 12, 10, 900.00,  'completed'),
(46, 13, 11, 140.00,  'completed'),
(47, 14, 12, 320.00,  'disputed'),
(48, 15, 13, 390.00,  'completed'),
(49, 1,  14, 210.00,  'completed'),
(50, 2,  15, 1050.00, 'pending');

-- ============================================
-- REVIEWS (for completed orders)
-- ============================================
INSERT INTO review (order_id, reviewer_id, reviewee_id, star_rating, comment) VALUES
(6,  3,  11, 5, 'Smooth transfer, flight ticket was exactly as described. Would buy again.'),
(7,  4,  12, 5, 'First class ticket transferred perfectly. Seller was very responsive.'),
(8,  5,  13, 4, 'Good communication, ticket arrived on time. Minor delay in confirmation email.'),
(9,  6,  14, 5, 'Excellent seller. Singapore Airlines seat was exactly as advertised.'),
(10, 7,  15, 5, 'Perfect transaction. Cathay Pacific first class — worth every penny.'),
(12, 9,  7,  4, 'Good seller, the concert ticket worked at the gate with no issues.'),
(13, 10, 8,  5, 'Great experience. Seller confirmed transfer within an hour of auction end.'),
(15, 12, 10, 5, 'Fantastic seller. Flight ticket transfer was seamless and fast.'),
(16, 13, 11, 3, 'Ticket was valid but seller took 2 days to confirm the transfer. Communication could be faster.'),
(18, 15, 13, 5, 'Great seller. Film festival ticket was genuine and transfer was instant.');

-- ============================================
-- TRUST SCORES for new users (id 6-15)
-- ============================================
INSERT INTO trust_score (user_id, score, total_auctions, successful_auctions, disputes) VALUES
(6,  97.00, 3, 3, 0),
(7,  94.50, 3, 3, 0),
(8,  96.00, 3, 3, 0),
(9,  91.00, 3, 3, 0),
(10, 93.00, 3, 3, 0),
(11, 88.50, 3, 2, 1),
(12, 95.00, 3, 3, 0),
(13, 82.00, 3, 2, 1),
(14, 78.00, 3, 2, 1),
(15, 96.50, 3, 3, 0);
