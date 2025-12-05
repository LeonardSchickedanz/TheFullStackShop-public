SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE OrderLine;
TRUNCATE TABLE `Order`;
TRUNCATE TABLE Product;
TRUNCATE TABLE Address;
TRUNCATE TABLE User;
TRUNCATE TABLE Brand;
TRUNCATE TABLE Category;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO Role (role_id, name)
VALUES (1, 'ADMIN')
ON DUPLICATE KEY UPDATE role_id=role_id;

INSERT INTO Role (role_id, name) VALUES (2, 'CUSTOMER');

INSERT INTO Category (category_id, name)
VALUES (201, 'Computers & Laptops'),
       (202, 'Tablets & Pads'),
       (203, 'Accessories');

INSERT INTO Brand (brand_id, name)
VALUES (101, 'ApexTech'),
       (102, 'OmniGear'),
       (103, 'Visionary');

INSERT INTO User (first_name, last_name, email, password, role_id)
VALUES ('Super', 'Admin', 'admin@shop.com', '$2a$10$XG3cWQjtA7DagEqw54YtBOVI1gRii1mgBUaMGwm.sGNqdKaaCK86m', 1);

INSERT INTO Address (street, street_number, postal_code, city, province, country, user_id)
VALUES ('Queen St W', '100', 'M5V 2H1', 'Toronto', 'ON', 'Canada', 1);


INSERT INTO Product (product_id, name, description, price, img_url, quantity, brand_id, category_id)
VALUES (1001, 'Smart Watch X1', 'Fitness tracker with ECG sensor and OLED display.', 199.99,
        '/images/products/watch_x1.jpg',
        50, 102, 203),
       (1002, 'Wireless Keyboard', 'Mechanical keys, RGB lighting, long battery life.', 75.50,
        '/images/products/keyboard.jpg',
        120, 102, 203),

       (1003, 'ApexBook Pro 15', 'High-performance laptop with 16GB RAM and 512GB SSD.', 1299.00,
        '/images/products/laptop_pro_15.jpg', 30, 101, 201),
       (1004, 'UltraBook Air 13', 'Ultra-thin and lightweight, perfect for travel.', 999.00,
        '/images/products/laptop_air_13.jpg', 45, 101, 201),
       (1005, 'Gaming Laptop Z', 'RTX 4060 Graphics, 144Hz Screen, RGB Keyboard.', 1450.00,
        '/images/products/laptop_gaming.jpg', 20, 101, 201),

       (1006, 'VisionPad 11', '11-inch Liquid Retina display, M2 chip equivalent.', 799.00,
        '/images/products/tablet_11.jpg',
        60, 103, 202),
       (1007, 'VisionPad Mini', '8.3-inch compact tablet, fits in one hand.', 499.00,
        '/images/products/tablet_mini.jpg', 85,
        103, 202),
       (1008, 'E-Reader Paper', 'Glare-free display, reads like real paper.', 129.00, '/images/products/ereader.jpg',
        100, 103,
        202),

       (1009, 'Wireless Ergonomic Mouse', 'Vertical design to reduce wrist strain.', 45.00,
        '/images/products/mouse_ergo.jpg',
        150, 102, 203),
       (1010, 'USB-C Docking Station', '7-in-1 Hub with HDMI, USB 3.0, and SD Card Reader.', 59.99,
        '/images/products/usbc_dock.jpg', 200, 102, 203),
       (1011, 'External SSD 1TB', 'High-speed portable storage, USB 3.2.', 119.00, '/images/products/ssd_1tb.jpg', 75,
        102,
        203),
       (1012, 'Noise-Cancelling Headphones', 'Over-ear wireless headphones with 30h battery.', 249.00,
        '/images/products/headphones_nc.jpg', 40, 103, 203),
       (1013, 'True Wireless Earbuds', 'Compact charging case, active noise cancellation.', 129.00,
        '/images/products/earbuds.jpg', 90, 103, 203),
       (1014, '27-inch 4K Monitor', 'IPS panel, perfect for creative work.', 399.00, '/images/products/monitor_4k.jpg',
        25,
        103, 201),
       (1015, 'Laptop Sleeve 15"', 'Water-resistant protective case for 15-inch laptops.', 29.99,
        '/images/products/sleeve_15.jpg', 300, 102, 203);