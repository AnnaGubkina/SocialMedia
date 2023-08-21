CREATE TABLE users(
    id bigint generated by default as identity primary key,
    username varchar(100) not null,
    password varchar(255) not null,
    email varchar(255) not null
);

create table roles
(

     id bigint generated by default as identity primary key,
     name varchar(255)

);

create table user_roles
(
    user_id bigint not null
        constraint user_id_fk
            references users ,
    role_id bigint not null
        constraint role_id_fk
            references roles ,
    constraint user_role
        primary key (user_id, role_id)
);

CREATE TABLE posts (
                       id bigint generated by default as identity primary key,
                       title VARCHAR(255) NOT NULL,
                       text TEXT NOT NULL,
                       file bytea,
                       user_id BIGINT NOT NULL,
                       date TIMESTAMP,
                       FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE followers (
                               id bigint generated by default as identity primary key,
                               sender_id BIGINT NOT NULL REFERENCES users(id),
                               receiver_id BIGINT NOT NULL REFERENCES users(id),
                               date TIMESTAMP DEFAULT NOW()
);



CREATE TABLE friendships (
                             id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                             user_one_id BIGINT NOT NULL REFERENCES users(id),
                             user_two_id BIGINT NOT NULL REFERENCES users(id),
                             date TIMESTAMP DEFAULT NOW()

);

CREATE TABLE messages (
    id bigint generated by default as identity primary key,
    sender_id bigint not null,
    receiver_id bigint not null,
    text varchar(1000) not null,
    date timestamp not null,
    foreign key (sender_id) references users (id),
    foreign key (receiver_id) references users (id)
);
