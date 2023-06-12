DROP TABLE IF EXISTS user_phones;
DROP TABLE IF EXISTS phones;
DROP TABLE IF EXISTS aardvark_users;

CREATE TABLE aardvark_users (

    id UUID PRIMARY KEY NOT NULL,
    name VARCHAR,
    password VARCHAR NOT NULL,
    created TIMESTAMP,
    last_login TIMESTAMP,
    email VARCHAR,
    isActive BOOLEAN,
    token VARCHAR(512)

);

CREATE TABLE phones (

    id UUID PRIMARY KEY NOT NULL,
    number LONG,
    city_code INT,
    country_code VARCHAR,
    user UUID,
    CONSTRAINT FK_USer FOREIGN KEY (user) REFERENCES aardvark_users(id)
);
