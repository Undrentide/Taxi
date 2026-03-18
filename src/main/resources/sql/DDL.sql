DROP SCHEMA IF EXISTS taxi;
CREATE SCHEMA IF NOT EXISTS taxi;
USE taxi;

CREATE TABLE IF NOT EXISTS `role`
(
    id   BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS `user`
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50),
    last_name  VARCHAR(50),
    phone      VARCHAR(20) UNIQUE,
    role_id    BIGINT,
    FOREIGN KEY (role_id) REFERENCES `role` (id)
);

CREATE TABLE IF NOT EXISTS driver_status
(
    id   BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS car_class
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    name       VARCHAR(50),
    base_price DECIMAL(10, 2)
);

CREATE TABLE IF NOT EXISTS car
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    brand         VARCHAR(50),
    model         VARCHAR(50),
    license_plate VARCHAR(20) UNIQUE,
    color         VARCHAR(30),
    class_id      BIGINT,
    FOREIGN KEY (class_id) REFERENCES car_class (id)
);

CREATE TABLE IF NOT EXISTS driver
(
    id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id   BIGINT,
    car_id    BIGINT,
    status_id BIGINT,
    rating    DECIMAL(3, 2) DEFAULT 5.0,
    FOREIGN KEY (user_id) REFERENCES `user` (id),
    FOREIGN KEY (car_id) REFERENCES car (id),
    FOREIGN KEY (status_id) REFERENCES driver_status (id)
);

CREATE TABLE IF NOT EXISTS payment_type
(
    id   BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS order_status
(
    id   BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS promo_code
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    code             VARCHAR(20) UNIQUE,
    discount_percent INT,
    is_active        BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS region
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    name       VARCHAR(100),
    multiplier DECIMAL(3, 2) DEFAULT 1.0
);

CREATE TABLE IF NOT EXISTS `order`
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    client_id     BIGINT,
    driver_id     BIGINT,
    status_id     BIGINT,
    promo_code_id BIGINT,
    region_id     BIGINT,
    from_address  VARCHAR(255),
    to_address    VARCHAR(255),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES `user` (id),
    FOREIGN KEY (driver_id) REFERENCES driver (id),
    FOREIGN KEY (status_id) REFERENCES order_status (id),
    FOREIGN KEY (promo_code_id) REFERENCES promo_code (id),
    FOREIGN KEY (region_id) REFERENCES region (id)
);

CREATE TABLE IF NOT EXISTS payment
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id        BIGINT,
    amount          DECIMAL(10, 2),
    payment_type_id BIGINT,
    paid_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES `order` (id),
    FOREIGN KEY (payment_type_id) REFERENCES payment_type (id)
);

CREATE TABLE IF NOT EXISTS review
(
    id       BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT,
    rating   INT CHECK (rating BETWEEN 1 AND 5),
    comment  TEXT,
    FOREIGN KEY (order_id) REFERENCES `order` (id)
);

CREATE TABLE IF NOT EXISTS support_ticket
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT,
    subject     VARCHAR(255),
    message     TEXT,
    is_resolved BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES `user` (id)
);

CREATE TABLE IF NOT EXISTS driver_location_log
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    driver_id  BIGINT,
    latitude   DECIMAL(10, 8),
    longitude  DECIMAL(11, 8),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (driver_id) REFERENCES driver (id)
)