# --- !Ups

create table "movie-director" (
	"id" bigserial primary key,
	"movie-id" bigint not null,
	"director-id" bigint not null
);

create table "movie-actor" (
	"id" bigserial primary key,
	"movie-id" bigint not null,
	"actor-id" bigint not null
);

create table "movie-genre" (
	"id" bigserial primary key,
	"movie-id" bigint not null,
	"genre-id" bigint not null
);

insert into "movie-director" ("movie-id", "director-id") values ('1', '1');
insert into "movie-director" ("movie-id", "director-id") values ('2', '1');
insert into "movie-director" ("movie-id", "director-id") values ('2', '2');

insert into "movie-actor" ("movie-id", "actor-id") values ('3', '1');
insert into "movie-actor" ("movie-id", "actor-id") values ('3', '3');
insert into "movie-actor" ("movie-id", "actor-id") values ('4', '2');
insert into "movie-actor" ("movie-id", "actor-id") values ('5', '1');

insert into "movie-genre" ("movie-id", "genre-id") values ('1', '1');
insert into "movie-genre" ("movie-id", "genre-id") values ('2', '4');
insert into "movie-genre" ("movie-id", "genre-id") values ('3', '2');
insert into "movie-genre" ("movie-id", "genre-id") values ('1', '3');

# --- !Downs

drop table if exists "movie-actor";
drop table if exists "movie-director";
drop table if exists "movie-genre";