USE taxi;

INSERT INTO `role` (name)
VALUES ('client'),
       ('driver'),
       ('admin');

INSERT INTO `user` (first_name, last_name, phone, role_id)
VALUES ('Alice', 'Smith', '+10000000001', 1),
       ('Bob', 'Jones', '+10000000002', 2),
       ('Carol', 'White', '+10000000003', 2),
       ('Dave', 'Brown', '+10000000004', 1);

INSERT INTO driver_status (name)
VALUES ('available'),
       ('busy'),
       ('offline');

INSERT INTO car_class (name, base_price)
VALUES ('Economy', 5.00),
       ('Comfort', 9.00),
       ('Business', 15.00);

INSERT INTO car (brand, model, license_plate, color, class_id)
VALUES ('Toyota', 'Corolla', 'AA1111BB', 'white', 1),
       ('Honda', 'Civic', 'BB2222CC', 'black', 2),
       ('BMW', '5 Series', 'CC3333DD', 'silver', 3);

INSERT INTO driver (user_id, car_id, status_id, rating)
VALUES (2, 1, 1, 4.80),
       (3, 2, 2, 4.50),
       (3, 3, 3, 4.95);

INSERT INTO payment_type (name)
VALUES ('cash'),
       ('card'),
       ('wallet');

INSERT INTO order_status (name)
VALUES ('pending'),
       ('in_progress'),
       ('completed'),
       ('cancelled');

INSERT INTO promo_code (code, discount_percent, is_active)
VALUES ('SAVE10', 10, TRUE),
       ('FIRST20', 20, TRUE),
       ('OFF5', 5, FALSE);

INSERT INTO region (name, multiplier)
VALUES ('Downtown', 1.20),
       ('Suburbs', 1.00),
       ('Airport', 1.50);

INSERT INTO `order` (client_id, driver_id, status_id, promo_code_id, region_id, from_address, to_address)
VALUES (1, 1, 3, 1, 1, '1 Main St', '5 Oak Ave'),
       (4, 2, 3, NULL, 2, '8 Pine Rd', '3 Elm St'),
       (1, 3, 2, 2, 3, '12 Airport Blvd', '7 City Sq'),
       (4, 1, 4, NULL, 1, '2 Park Lane', '9 River Rd');

INSERT INTO payment (order_id, amount, payment_type_id)
VALUES (1, 12.00, 2),
       (2, 9.50, 1),
       (3, 22.50, 3);

INSERT INTO review (order_id, rating, comment)
VALUES (1, 5, 'Great ride!'),
       (2, 4, 'Comfortable, a bit late.'),
       (3, 5, 'Perfect service.');

INSERT INTO support_ticket (user_id, subject, message, is_resolved)
VALUES (1, 'App crash', 'The app crashed on payment.', FALSE),
       (4, 'Wrong charge', 'I was overcharged by $2.', TRUE),
       (2, 'Lost item', 'Left my bag in the car.', FALSE);

INSERT INTO driver_location_log (driver_id, latitude, longitude)
VALUES (1, 40.71277500, -74.00597200),
       (2, 34.05223400, -118.24368500),
       (3, 51.50735400, -0.12775800);