# --- !Ups

truncate "movie", "director", "actor";

alter table "movie" alter column "country" type varchar(2);
alter table "director" alter column "nationality" type varchar(2);
alter table "actor" alter column "nationality" type varchar(2);

insert into "actor" ("first-name", "last-name", "birth-date", "nationality", "height", "gender")
	values ('John', 'Green', '2000-12-15', 'AU', '190', 'm');
insert into "actor" ("first-name", "last-name", "birth-date", "nationality", "height", "gender")
	values ('Cameron', 'Frisbee', '1981-10-27', 'NZ', '174', 'm');
insert into "actor" ("first-name", "last-name", "birth-date", "nationality", "height", "gender")
	values ('Melissa', 'Fallon', '2002-09-01', 'US', '161', 'f');

insert into "director" ("first-name", "last-name", "birth-date", "nationality", "height", "gender")
	values ('Kevin', 'Rodriguez', '1964-01-16', 'ES', '185', 'm');
insert into "director" ("first-name", "last-name", "birth-date", "nationality", "height", "gender")
	values ('Clementine', 'Johannes', '1989-04-18', 'NZ', '178', 'f');

insert into "movie" ("title", "description", "release-date", "country", "language")
	values ('Girl with oranges', 'Dads letter to his son about his love story with mum', '2014-06-15', 'SE', 'sw');
insert into "movie" ("title", "description", "release-date", "country", "language")
	values ('Poland is our enemy', 'Lithuanians outlook on Poland', '1998-04-21', 'LT', 'lt');
insert into "movie" ("title", "description", "release-date", "country", "language")
	values ('Call me by your name', 'Story about two men romance during summer in Italy', '2018-08-01', 'IT', 'en');
insert into "movie" ("title", "description", "release-date", "country", "language")
	values ('Christmas carols', 'Your typical christmas movie', '2010-12-10', 'US', 'en');
insert into "movie" ("title", "description", "release-date", "country", "language")
	values ('Babushki', 'No one would like it', '2009-06-14', 'LT', 'ru');

# --- !Downs

truncate "movie", "director", "actor";

alter table "movie" alter column "country" type varchar;
alter table "director" alter column "nationality" type varchar;
alter table "actor" alter column "nationality" type varchar;

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