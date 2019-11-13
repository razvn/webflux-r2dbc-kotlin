/*
CREATE TABLE users (
                id SERIAL PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                login VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL,
                avatar VARCHAR(255)
);

 */
truncate table users;

INSERT INTO users(`id`, `name`, `login`, `email`, `avatar`)
values (1, 'User no 1', 'user1', 'user1@users.com', 'user1.png'),
       (2, 'User no 2', 'user2', 'user2@users.com', 'user2.png'),
       (3, 'User no 3', 'user3', 'user3@users.com', 'user3.png'),
       (4, 'User no 4', 'user4', 'user4@users.com', 'user4.png');