-- The starting sports catalog. This is real reference data that every environment needs, so it
-- belongs in a migration. To add or change a sport later, write a new migration.

INSERT INTO sports (slug, name, description, icon, display_order, created_at, updated_at) VALUES
('football',   'Football',   'Dribbling, passing, shooting and match fitness, from first touches to academy trials.', '⚽', 1, NOW(6), NOW(6)),
('cricket',    'Cricket',    'Batting technique, bowling actions and fielding drills with nets practice.',             '🏏', 2, NOW(6), NOW(6)),
('tennis',     'Tennis',     'Forehand, backhand, serve and footwork for beginners and competitive players.',          '🎾', 3, NOW(6), NOW(6)),
('badminton',  'Badminton',  'Grip, smashes, drops and court movement for singles and doubles.',                      '🏸', 4, NOW(6), NOW(6)),
('swimming',   'Swimming',   'Learn to swim safely or improve freestyle, breaststroke and endurance.',                 '🏊', 5, NOW(6), NOW(6)),
('fitness',    'Fitness',    'Strength, conditioning and weight-loss plans with a personal trainer.',                  '💪', 6, NOW(6), NOW(6)),
('yoga',       'Yoga',       'Flexibility, balance and breathing, from gentle Hatha to dynamic Vinyasa.',               '🧘', 7, NOW(6), NOW(6)),
('basketball', 'Basketball', 'Shooting form, ball handling and team play for every level.',                            '🏀', 8, NOW(6), NOW(6));
