create table if not exists public."user"
(
    email          varchar(128),
    first_name     varchar(64)  not null,
    last_name      varchar(64)  not null,
    phone_number   bigint       not null
    primary key,
    date_of_birth  date,
    date_of_signup date         not null,
    password       varchar(256) not null,
    salt           varchar(256) not null
);

alter table public."user"
    owner to postgres;
