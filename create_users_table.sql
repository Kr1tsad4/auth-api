create table users
(
    id           INT PRIMARY KEY AUTO_INCREMENT,
    email        VARCHAR(100) UNIQUE NOT NULL,
    password     VARCHAR(255)        NOT NULL,
    full_name    VARCHAR(100)        NOT NULL,
    dob          DATE                NOT NULL,
    phone_number VARCHAR(20),
    status       VARCHAR(50)         NOT NULL,
    role         VARCHAR(20)         NOT NULL,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);