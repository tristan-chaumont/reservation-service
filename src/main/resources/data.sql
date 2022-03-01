INSERT INTO Train(train_id)
VALUES ('1'), ('2'), ('3'), ('4');

INSERT INTO Trip(trip_id, train_id, departure_city, arrival_city, departure_time, arrival_time, price)
VALUES ('1', '1', 'NANCY', 'METZ', '12:50:00', '13:28:00', 10),
       ('2', '1', 'METZ', 'NANCY', '16:32:00', '17:10:00', 10),
       ('3', '2', 'METZ', 'THIONVILLE', '13:33:00', '13:56:00', 8),
       ('4', '3', 'PONT A MOUSSON', 'Nancy', '14:23:00', '14:40:00', 5);

INSERT INTO Traveler(traveler_id, name)
VALUES ('1', 'Chaumont'),
       ('2', 'Noirot');

INSERT INTO Reservation(reservation_id, traveler_id, trip_id, window_seat)
VALUES ('1', '1', '1', true),
       ('2', '1', '2', false);
