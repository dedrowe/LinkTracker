create table tg_chats (
    id bigserial primary key,
    chat_id bigint unique not null,
    deleted boolean default false
);

create table links (
    id bigserial primary key,
    link varchar(512) unique not null,
    last_update timestamp not null,
    deleted boolean default false,
    checking boolean default false
);

create index idx_links_last_update_deleted_btree on links using btree (last_update) where deleted = false;

create table links_data (
    id bigserial primary key,
    link_id bigserial references links (id) not null,
    chat_id bigserial references tg_chats (id) not null,
    deleted boolean default false,
    unique (link_id, chat_id)
);

create index idx_links_data_id_link_id_deleted_btree on links_data using btree (link_id, deleted, id);

create table tags (
    id bigserial primary key,
    tag varchar(16) not null unique
);

create index idx_tags_tag_btree on tags using btree (tag);

create table links_data_to_tags (
    id bigserial primary key,
    data_id bigserial references links_data (id) not null,
    tag_id bigserial references  tags (id) not null
);

create index idx_links_data_to_tags_data_id_btree on links_data_to_tags using btree (data_id);

create table filters (
    id bigserial primary key,
    data_id bigserial references links_data (id) not null,
    filter varchar(32) not null
);

create index idx_filters_data_id_btree on filters using btree (data_id);
