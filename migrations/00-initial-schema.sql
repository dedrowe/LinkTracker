create table tg_chats (
    id bigserial primary key,
    chat_id bigint unique not null,
    deleted boolean default false
);

create table links (
    id bigserial primary key,
    link varchar(512) unique not null,
    last_update timestamp not null,
    deleted boolean default false
);

create table links_data (
    id bigserial primary key,
    link_id bigserial references links (id) not null,
    chat_id bigserial references tg_chats (id) not null,
    deleted boolean default false
);

create table tags (
    id bigserial primary key,
    tag varchar(16) not null unique
);

create table links_data_to_tags (
    id bigserial primary key,
    data_id bigserial references links_data (id) not null,
    tag_id bigserial references  tags (id) not null,
    deleted boolean default false
);

create table filters (
    id bigserial primary key,
    data_id bigserial references links_data (id) not null,
    filter varchar(32) not null
);
