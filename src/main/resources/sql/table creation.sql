CREATE TABLE Role
(
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    name    VARCHAR(50) NOT NULL
);

CREATE TABLE Brand
(
    brand_id INT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(100) NOT NULL
);

CREATE TABLE Category
(
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL
);

CREATE TABLE User
(
    user_id    INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    email      VARCHAR(150) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role_id    INT          NOT NULL,
    FOREIGN KEY (role_id) REFERENCES Role (role_id)
);

CREATE TABLE PaymentInfo
(
    payment_info_id INT AUTO_INCREMENT PRIMARY KEY,
    card_number     VARCHAR(255) NOT NULL,
    expiry_date     VARCHAR(10)  NOT NULL,
    cvc             VARCHAR(3) NOT NULL,
    user_id         INT          NOT NULL,
    last_used       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User (user_id) ON DELETE CASCADE
);

/* Depends on: Brand, Category */
CREATE TABLE Product
(
    product_id  INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150)   NOT NULL,
    description TEXT,
    price       DECIMAL(10, 2) NOT NULL,
    img_url     VARCHAR(255),
    quantity    INT            NOT NULL DEFAULT 0,
    brand_id    INT            NOT NULL,
    category_id INT            NOT NULL,
    FOREIGN KEY (brand_id) REFERENCES Brand (brand_id),
    FOREIGN KEY (category_id) REFERENCES Category (category_id)
);

/* Depends on: User */
CREATE TABLE Address
(
    address_id    INT AUTO_INCREMENT PRIMARY KEY,
    street        VARCHAR(150) NOT NULL,
    street_number VARCHAR(20)  NOT NULL,
    postal_code   VARCHAR(20)  NOT NULL,
    city          VARCHAR(100) NOT NULL,
    province      VARCHAR(50)  NOT NULL,
    country       VARCHAR(50)  NOT NULL DEFAULT 'Canada',
    user_id       INT          NOT NULL,
    last_used     TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User (user_id) ON DELETE CASCADE
);

/* Depends on: User */
CREATE TABLE ShoppingCart
(
    shopping_cart_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT          NULL,
    session_id       VARCHAR(100) NULL,
    last_updated     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User (user_id) ON DELETE CASCADE
);

/* Depends on: ShoppingCart, Product */
CREATE TABLE CartItem
(
    cart_item_id     INT AUTO_INCREMENT PRIMARY KEY,
    quantity         INT NOT NULL DEFAULT 1,
    shopping_cart_id INT NOT NULL,
    product_id       INT NOT NULL,
    FOREIGN KEY (shopping_cart_id) REFERENCES ShoppingCart (shopping_cart_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Product (product_id)
);

/* Depends on: User */
CREATE TABLE `Order`
(
    order_id         INT AUTO_INCREMENT PRIMARY KEY,
    transaction_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_price      DECIMAL(10, 2) NOT NULL,
    user_id          INT            NOT NULL,
    address_id       INT            NOT NULL,
    FOREIGN KEY (user_id) REFERENCES User (user_id),
    FOREIGN KEY (address_id) REFERENCES Address (address_id)
);

/*  Depends on: Order, Product*/
CREATE TABLE OrderLine
(
    order_line_id INT AUTO_INCREMENT PRIMARY KEY,
    quantity      INT            NOT NULL,
    unit_price    DECIMAL(10, 2) NOT NULL,
    order_id      INT            NOT NULL,
    product_id    INT            NOT NULL,
    FOREIGN KEY (order_id) REFERENCES `Order` (order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Product (product_id)
);


