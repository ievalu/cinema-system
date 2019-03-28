# --- !Ups

create table "movie" (
	"id" bigserial primary key,
	"title" varchar not null,
	"description" text not null,
	"release-date" date not null,
	"country" varchar not null,
	"language" varchar(2) not null
);

create table "director" (
	"id" bigserial primary key,
	"first-name" varchar not null,
	"last-name" varchar not null,
	"birth-date" date not null,
	"nationality" varchar not null,
	"height" int not null,
	"gender" varchar(1) not null
);

create table "actor" (
	"id" bigserial primary key,
	"first-name" varchar not null,
	"last-name" varchar not null,
	"birth-date" date not null,
	"nationality" varchar not null,
	"height" int not null,
	"gender" varchar(1) not null
);

create table "genre" (
	"id" bigserial primary key,
	"title" varchar not null
);

insert into "genre" ("title") values ('comedy');
insert into "genre" ("title") values ('romance');
insert into "genre" ("title") values ('horror');
insert into "genre" ("title") values ('drama');
insert into "genre" ("title") values ('action');

insert into "actor" ("first-name", "last-name", "birth-date", "nationality", "height", "gender")
	values ('John', 'Green', '2000-12-15', '40', '190', 'm');
insert into "actor" ("first-name", "last-name", "birth-date", "nationality", "height", "gender")
	values ('Cameron', 'Frisbee', '1981-10-27', '2', '174', 'm');
insert into "actor" ("first-name", "last-name", "birth-date", "nationality", "height", "gender")
	values ('Melissa', 'Fallon', '2002-09-01', '40', '161', 'f');

insert into "director" ("first-name", "last-name", "birth-date", "nationality", "height", "gender")
	values ('Kevin', 'Rodriguez', '1964-01-16', '32', '185', 'm');
insert into "director" ("first-name", "last-name", "birth-date", "nationality", "height", "gender")
	values ('Clementine', 'Johannes', '1989-04-18', '23', '178', 'f');

insert into "movie" ("title", "description", "release-date", "country", "language")
	values ('Girl with oranges', 'Dads letter to his son about his love story with mum', '2014-06-15', '34', 'sw');
insert into "movie" ("title", "description", "release-date", "country", "language")
	values ('Poland is our enemy', 'Lithuanians outlook on Poland', '1998-04-21', '97', 'lt');
insert into "movie" ("title", "description", "release-date", "country", "language")
	values ('Call me by your name', 'Story about two men romance during summer in Italy', '2018-08-01', '40', 'en');
insert into "movie" ("title", "description", "release-date", "country", "language")
	values ('Christmas carols', 'Your typical christmas movie', '2010-12-10', '40', 'en');
insert into "movie" ("title", "description", "release-date", "country", "language")
	values ('Babushki', 'No one would like it', '2009-06-14', '97', 'ru');

# --- !Downs

drop table if exists "movie";
drop table if exists "director";
drop table if exists "actor";
drop table if exists "genre";