INSERT INTO Trip(trip_id, departure_city, arrival_city, departure_time, arrival_time, price, num_corridor, num_window) VALUES ('1', 'NANCY', 'METZ', '2022-03-25T12:50:00', '2022-03-25T13:28:00', 10, 50, 50);
INSERT INTO Trip(trip_id, departure_city, arrival_city, departure_time, arrival_time, price, num_corridor, num_window) VALUES ('2', 'METZ', 'NANCY', '2022-03-25T16:32:00', '2022-03-25T17:10:00', 9, 50, 50);
INSERT INTO Trip(trip_id, departure_city, arrival_city, departure_time, arrival_time, price, num_corridor, num_window) VALUES ('5', 'METZ', 'NANCY', '2022-03-25T09:07:00', '2022-03-25T09:45:00', 10, 50, 50);
INSERT INTO Trip(trip_id, departure_city, arrival_city, departure_time, arrival_time, price, num_corridor, num_window) VALUES ('3', 'METZ', 'THIONVILLE', '2022-03-25T13:33:00', '2022-03-25T13:56:00', 8, 50, 50);
INSERT INTO Trip(trip_id, departure_city, arrival_city, departure_time, arrival_time, price, num_corridor, num_window) VALUES ('4', 'PONT-A-MOUSSON', 'NANCY', '2022-03-25T14:23:00', '2022-03-25T14:40:00', 5, 50, 50);
INSERT INTO Trip(trip_id, departure_city, arrival_city, departure_time, arrival_time, price, num_corridor, num_window) VALUES ('6', 'NANCY', 'METZ', '2022-03-26T14:23:00', '2022-03-26T14:40:00', 5, 50, 50);

INSERT INTO Traveler(traveler_id, name) VALUES ('1', 'Chaumont');
INSERT INTO Traveler(traveler_id, name) VALUES ('2', 'Noirot');

INSERT INTO Reservation(reservation_id, traveler_id, trip_id, window_seat, status) VALUES ('1', '1', '1', true, 'PENDING');
INSERT INTO Reservation(reservation_id, traveler_id, trip_id, window_seat, status) VALUES ('2', '1', '2', false, 'PENDING');
