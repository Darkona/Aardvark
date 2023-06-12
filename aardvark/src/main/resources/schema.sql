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
    isActive BOOLEAN

);

CREATE TABLE phones (

    id UUID PRIMARY KEY NOT NULL,
    number LONG,
    city_code INT,
    country_code VARCHAR
);

CREATE TABLE user_phones (

    user_id UUID NOT NULL,
    phone_id UUID NOT NULL,
    PRIMARY KEY (user_id, phone_id),
    FOREIGN KEY (user_id) REFERENCES aardvark_users(id),
    FOREIGN KEY (phone_id) REFERENCES phones(id)
);