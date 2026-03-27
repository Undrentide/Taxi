USE taxi;

INSERT INTO `role` (id, name)
VALUES ('9338271e-966a-4933-97ba-24819d936173', 'CLIENT'),
       ('6b90757b-7b00-47b1-9b19-f542459048a1', 'DRIVER'),
       ('e344795e-1493-4171-a400-f7091171a400', 'ADMIN');

INSERT INTO `user` (id, first_name, last_name, phone, role_id)
VALUES ('a100757b-7b00-47b1-9b19-f542459048a1', 'John', 'Doe', '+15551002001', '9338271e-966a-4933-97ba-24819d936173'),
       ('a200757b-7b00-47b1-9b19-f542459048a2', 'Jane', 'Smith', '+15551002002',
        '9338271e-966a-4933-97ba-24819d936173'),
       ('a300757b-7b00-47b1-9b19-f542459048a3', 'Alice', 'Brown', '+15551002003',
        '9338271e-966a-4933-97ba-24819d936173'),
       ('d100757b-7b00-47b1-9b19-f542459048d1', 'Robert', 'Miller', '+15552003001',
        '6b90757b-7b00-47b1-9b19-f542459048a1'),
       ('d200757b-7b00-47b1-9b19-f542459048d2', 'Michael', 'Wilson', '+15552003002',
        '6b90757b-7b00-47b1-9b19-f542459048a1'),
       ('d300757b-7b00-47b1-9b19-f542459048d3', 'David', 'Davis', '+15552003003',
        '6b90757b-7b00-47b1-9b19-f542459048a1');

INSERT INTO driver_status (id, name)
VALUES ('100757b0-7b00-47b1-9b19-f542459048a1', 'Available'),
       ('200757b0-7b00-4771-9b19-f542459048a2', 'Busy'),
       ('300757b0-7b00-47b1-9b19-f542459048a3', 'Offline');

INSERT INTO car_class (id, name, base_price)
VALUES ('c100757b-7b00-47b1-9b19-f542459048c1', 'Economy', 10.00),
       ('c200757b-7b00-47b1-9b19-f542459048c2', 'Standard', 15.00),
       ('c300757b-7b00-47b1-9b19-f542459048c3', 'Business', 25.00);

INSERT INTO car (id, brand, model, license_plate, color, class_id)
VALUES ('b100757b-7b00-47b1-9b19-f542459048b1', 'Toyota', 'Camry', 'NY1001', 'Black',
        'c300757b-7b00-47b1-9b19-f542459048c3'),
       ('b200757b-7b00-47b1-9b19-f542459048b2', 'Hyundai', 'Elantra', 'NY2002', 'White',
        'c100757b-7b00-47b1-9b19-f542459048c1'),
       ('b300757b-7b00-47b1-9b19-f542459048b3', 'Skoda', 'Octavia', 'NY3003', 'Silver',
        'c200757b-7b00-47b1-9b19-f542459048c2');

INSERT INTO driver (id, user_id, car_id, status_id, rating)
VALUES ('f10757b0-7b00-47b1-9b19-f542459048f1', 'd100757b-7b00-47b1-9b19-f542459048d1',
        'b100757b-7b00-47b1-9b19-f542459048b1', '100757b0-7b00-47b1-9b19-f542459048a1', 4.90),
       ('f20757b0-7b00-47b1-9b19-f542459048f2', 'd200757b-7b00-47b1-9b19-f542459048d2',
        'b200757b-7b00-47b1-9b19-f542459048b2', '100757b0-7b00-47b1-9b19-f542459048a1', 4.80),
       ('f30757b0-7b00-47b1-9b19-f542459048f3', 'd300757b-7b00-47b1-9b19-f542459048d3',
        'b300757b-7b00-47b1-9b19-f542459048b3', '200757b0-7b00-4771-9b19-f542459048a2', 5.00);

INSERT INTO payment_type (id, name)
VALUES ('e100757b-7b00-47b1-9b19-f542459048e1', 'Cash'),
       ('e200757b-7b00-47b1-9b19-f542459048e2', 'Credit Card'),
       ('e300757b-7b00-47b1-9b19-f542459048e3', 'Apple Pay');

INSERT INTO order_status (id, name)
VALUES ('f100757b-7b00-47b1-9b19-f542459048f1', 'Completed'),
       ('f200757b-7b00-47b1-9b19-f542459048f2', 'Accepted'),
       ('f300757b-7b00-47b1-9b19-f542459048f3', 'Cancelled');

INSERT INTO promo_code (id, code, discount_percent, is_active)
VALUES ('e400757b-7b00-47b1-9b19-f542459048e4', 'FIRST10', 10, 1),
       ('e500757b-7b00-47b1-9b19-f542459048e5', 'CITY20', 20, 1),
       ('e600757b-7b00-47b1-9b19-f542459048e6', 'PROMO50', 50, 0);

INSERT INTO region (id, name, multiplier)
VALUES ('e700757b-7b00-47b1-9b19-f542459048e7', 'Downtown', 1.50),
       ('e800757b-7b00-47b1-9b19-f542459048e8', 'Airport', 2.00),
       ('e900757b-7b00-47b1-9b19-f542459048e9', 'Suburbs', 1.00);

INSERT INTO `order` (id, client_id, driver_id, status_id, promo_code_id, region_id, from_address, to_address)
VALUES ('ba00757b-7b00-47b1-9b19-f542459048a1', 'a100757b-7b00-47b1-9b19-f542459048a1',
        'f10757b0-7b00-47b1-9b19-f542459048f1', 'f100757b-7b00-47b1-9b19-f542459048f1',
        'e400757b-7b00-47b1-9b19-f542459048e4', 'e700757b-7b00-47b1-9b19-f542459048e7', '100 Broadway',
        '500 Fifth Ave'),
       ('ba00757b-7b00-47b1-9b19-f542459048a2', 'a200757b-7b00-47b1-9b19-f542459048a2',
        'f20757b0-7b00-47b1-9b19-f542459048f2', 'f100757b-7b00-47b1-9b19-f542459048f1', NULL,
        'e800757b-7b00-47b1-9b19-f542459048e8', 'JFK Terminal 4', 'Central Park South'),
       ('ba00757b-7b00-47b1-9b19-f542459048a3', 'a300757b-7b00-47b1-9b19-f542459048a3',
        'f10757b0-7b00-47b1-9b19-f542459048f1', 'f300757b-7b00-47b1-9b19-f542459048f3', NULL,
        'e900757b-7b00-47b1-9b19-f542459048e9', 'Library', 'Hotel');

INSERT INTO payment (id, order_id, amount, payment_type_id)
VALUES ('ca00757b-7b00-47b1-9b19-f542459048a1', 'ba00757b-7b00-47b1-9b19-f542459048a1', 15.00,
        'e200757b-7b00-47b1-9b19-f542459048e2'),
       ('ca00757b-7b00-47b1-9b19-f542459048a2', 'ba00757b-7b00-47b1-9b19-f542459048a2', 40.00,
        'e100757b-7b00-47b1-9b19-f542459048e1'),
       ('ca00757b-7b00-47b1-9b19-f542459048a3', 'ba00757b-7b00-47b1-9b19-f542459048a1', 5.00,
        'e100757b-7b00-47b1-9b19-f542459048e1');

INSERT INTO review (id, order_id, rating, comment)
VALUES ('da00757b-7b00-47b1-9b19-f542459048a1', 'ba00757b-7b00-47b1-9b19-f542459048a1', 5, 'Perfect ride.'),
       ('da00757b-7b00-47b1-9b19-f542459048a2', 'ba00757b-7b00-47b1-9b19-f542459048a2', 4, 'Polite driver.'),
       ('da00757b-7b00-47b1-9b19-f542459048a3', 'ba00757b-7b00-47b1-9b19-f542459048a1', 5, 'Great car.');

INSERT INTO support_ticket (id, user_id, subject, message, is_resolved)
VALUES ('ea00757b-7b00-47b1-9b19-f542459048a1', 'a100757b-7b00-47b1-9b19-f542459048a1', 'Payment issue',
        'Charged twice for the trip.', 1),
       ('ea00757b-7b00-47b1-9b19-f542459048a2', 'a200757b-7b00-47b1-9b19-f542459048a2', 'Lost item',
        'Left phone in the car.', 0),
       ('ea00757b-7b00-47b1-9b19-f542459048a3', 'd100757b-7b00-47b1-9b19-f542459048d1', 'App error',
        'Cannot update status.', 0);

INSERT INTO driver_location_log (id, driver_id, latitude, longitude)
VALUES ('aa00757b-7b00-47b1-9b19-f542459048a1', 'f10757b0-7b00-47b1-9b19-f542459048f1', 40.7128, -74.0060),
       ('aa00757b-7b00-47b1-9b19-f542459048a2', 'f20757b0-7b00-47b1-9b19-f542459048f2', 40.7580, -73.9855),
       ('aa00757b-7b00-47b1-9b19-f542459048a3', 'f10757b0-7b00-47b1-9b19-f542459048f1', 40.7829, -73.9654);