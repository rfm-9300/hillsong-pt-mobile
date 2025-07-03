-- V2__Alter_user_table.sql

-- Rename the column 'email' to 'username'
ALTER TABLE public."user"
RENAME COLUMN email TO username;

-- Drop the columns that are no longer needed
ALTER TABLE public."user"
DROP COLUMN first_name,
DROP COLUMN last_name,
DROP COLUMN phone_number,
DROP COLUMN date_of_birth,
DROP COLUMN date_of_signup;

-- Modify the primary key to be on 'username'
ALTER TABLE public."user"
DROP CONSTRAINT IF EXISTS user_pkey,   -- Drop existing primary key if it exists
ADD PRIMARY KEY (username);            -- Add primary key on 'username'
