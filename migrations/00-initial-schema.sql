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
    tags varchar(16)[] not null default array[]::varchar[],
    filters varchar(32)[] not null default array[]::varchar[],
    deleted boolean default false
);
