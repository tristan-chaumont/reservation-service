INSERT INTO Train(train_id) VALUES ('1');
INSERT INTO Train(train_id) VALUES ('2');
INSERT INTO Train(train_id) VALUES ('3');
INSERT INTO Train(train_id) VALUES ('4');

INSERT INTO Trip(trip_id, train_id, departure_city, arrival_city, departure_time, arrival_time, price) VALUES ('1', '1', 'NANCY', 'METZ', '2022-03-25T12:50:00', '2022-03-25T13:28:00', 10);
INSERT INTO Trip(trip_id, train_id, departure_city, arrival_city, departure_time, arrival_time, price) VALUES ('2', '1', 'METZ', 'NANCY', '2022-03-25T16:32:00', '2022-03-25T17:10:00', 10);
INSERT INTO Trip(trip_id, train_id, departure_city, arrival_city, departure_time, arrival_time, price) VALUES ('5', '1', 'METZ', 'NANCY', '2022-03-25T09:07:00', '2022-03-25T09:45:00', 10);
INSERT INTO Trip(trip_id, train_id, departure_city, arrival_city, departure_time, arrival_time, price) VALUES ('3', '2', 'METZ', 'THIONVILLE', '2022-03-25T13:33:00', '2022-03-25T13:56:00', 8);
INSERT INTO Trip(trip_id, train_id, departure_city, arrival_city, departure_time, arrival_time, price) VALUES ('4', '3', 'PONT-A-MOUSSON', 'NANCY', '2022-03-25T14:23:00', '2022-03-25T14:40:00', 5);

INSERT INTO Traveler(traveler_id, name) VALUES ('1', 'Chaumont');
INSERT INTO Traveler(traveler_id, name) VALUES ('2', 'Noirot');

INSERT INTO Reservation(reservation_id, traveler_id, trip_id, window_seat) VALUES ('1', '1', '1', true);
INSERT INTO Reservation(reservation_id, traveler_id, trip_id, window_seat) VALUES ('2', '1', '2', false);
