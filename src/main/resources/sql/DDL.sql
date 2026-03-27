DROP SCHEMA IF EXISTS taxi;
CREATE SCHEMA IF NOT EXISTS taxi;
USE taxi;

CREATE TABLE IF NOT EXISTS `role`
(
    id   VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS `user`
(
    id         VARCHAR(36) PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name  VARCHAR(50) NOT NULL,
    phone      VARCHAR(20) NOT NULL UNIQUE,
    role_id    VARCHAR(36) NOT NULL,
    FOREIGN KEY (role_id) REFERENCES `role` (id)
);

CREATE TABLE IF NOT EXISTS driver_status
(
    id   VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS car_class
(
    id         VARCHAR(36) PRIMARY KEY,
    name       VARCHAR(50)    NOT NULL,
    base_price DECIMAL(10, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS car
(
    id            VARCHAR(36) PRIMARY KEY,
    brand         VARCHAR(50) NOT NULL,
    model         VARCHAR(50) NOT NULL,
    license_plate VARCHAR(20) NOT NULL UNIQUE,
    color         VARCHAR(30) NOT NULL,
    class_id      VARCHAR(36) NOT NULL,
    FOREIGN KEY (class_id) REFERENCES car_class (id)
);

CREATE TABLE IF NOT EXISTS driver
(
    id        VARCHAR(36) PRIMARY KEY,
    user_id   VARCHAR(36)               NOT NULL,
    car_id    VARCHAR(36)               NOT NULL,
    status_id VARCHAR(36)               NOT NULL,
    rating    DECIMAL(3, 2) DEFAULT 5.0 NOT NULL,
    FOREIGN KEY (user_id) REFERENCES `user` (id),
    FOREIGN KEY (car_id) REFERENCES car (id),
    FOREIGN KEY (status_id) REFERENCES driver_status (id)
);

CREATE TABLE IF NOT EXISTS payment_type
(
    id   VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS order_status
(
    id   VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS promo_code
(
    id               VARCHAR(36) PRIMARY KEY,
    code             VARCHAR(20)          NOT NULL UNIQUE,
    discount_percent INT                  NOT NULL,
    is_active        BOOLEAN DEFAULT TRUE NOT NULL
);

CREATE TABLE IF NOT EXISTS region
(
    id         VARCHAR(36) PRIMARY KEY,
    name       VARCHAR(100)              NOT NULL,
    multiplier DECIMAL(3, 2) DEFAULT 1.0 NOT NULL
);

CREATE TABLE IF NOT EXISTS `order`
(
    id            VARCHAR(36) PRIMARY KEY,
    client_id     VARCHAR(36)                         NOT NULL,
    driver_id     VARCHAR(36)                         NOT NULL,
    status_id     VARCHAR(36)                         NOT NULL,
    promo_code_id VARCHAR(36),
    region_id     VARCHAR(36)                         NOT NULL,
    from_address  VARCHAR(255)                        NOT NULL,
    to_address    VARCHAR(255)                        NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (client_id) REFERENCES `user` (id),
    FOREIGN KEY (driver_id) REFERENCES driver (id),
    FOREIGN KEY (status_id) REFERENCES order_status (id),
    FOREIGN KEY (promo_code_id) REFERENCES promo_code (id),
    FOREIGN KEY (region_id) REFERENCES region (id)
);

CREATE TABLE IF NOT EXISTS payment
(
    id              VARCHAR(36) PRIMARY KEY,
    order_id        VARCHAR(36)                         NOT NULL,
    amount          DECIMAL(10, 2)                      NOT NULL,
    payment_type_id VARCHAR(36)                         NOT NULL,
    paid_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (order_id) REFERENCES `order` (id),
    FOREIGN KEY (payment_type_id) REFERENCES payment_type (id)
);

CREATE TABLE IF NOT EXISTS review
(
    id       VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36)                        NOT NULL,
    rating   INT CHECK (rating BETWEEN 1 AND 5) NOT NULL,
    comment  TEXT,
    FOREIGN KEY (order_id) REFERENCES `order` (id)
);

CREATE TABLE IF NOT EXISTS support_ticket
(
    id          VARCHAR(36) PRIMARY KEY,
    user_id     VARCHAR(36)           NOT NULL,
    subject     VARCHAR(255)          NOT NULL,
    message     TEXT                  NOT NULL,
    is_resolved BOOLEAN DEFAULT FALSE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES `user` (id)
);

CREATE TABLE IF NOT EXISTS driver_location_log
(
    id         VARCHAR(36) PRIMARY KEY,
    driver_id  VARCHAR(36)                         NOT NULL,
    latitude   DECIMAL(10, 8)                      NOT NULL,
    longitude  DECIMAL(11, 8)                      NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (driver_id) REFERENCES driver (id)
)