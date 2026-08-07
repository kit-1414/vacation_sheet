CREATE TABLE user_accounts (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE CHECK (email = lower(btrim(email))),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    ctime TIMESTAMP WITH TIME ZONE NOT NULL,
    utime TIMESTAMP WITH TIME ZONE NOT NULL
);
