# --- !Ups

alter table "movie" alter column "country" type varchar(2);
alter table "director" alter column "nationality" type varchar(2);
alter table "actor" alter column "nationality" type varchar(2);

update "actor" SET "nationality" = 'AU' WHERE "id" = '1';
update "actor" SET "nationality" = 'NZ' WHERE "id" = '2';
update "actor" SET "nationality" = 'US' WHERE "id" = '3';

update "director" SET "nationality" = 'ES' WHERE "id" = '1';
update "director" SET "nationality" = 'NZ' WHERE "id" = '2';

update "movie" SET "country" = 'SE' WHERE "id" = '1';
update "movie" SET "country" = 'LT' WHERE "id" = '2';
update "movie" SET "country" = 'IT' WHERE "id" = '3';
update "movie" SET "country" = 'US' WHERE "id" = '4';
update "movie" SET "country" = 'LT' WHERE "id" = '5';

# --- !Downs

alter table "movie" alter column "country" type varchar;
alter table "director" alter column "nationality" type varchar;
alter table "actor" alter column "nationality" type varchar;

update "actor" SET "nationality" = '40' WHERE "id" = '1';
update "actor" SET "nationality" = '2' WHERE "id" = '2';
update "actor" SET "nationality" = '40' WHERE "id" = '3';

update "director" SET "nationality" = '32' WHERE "id" = '1';
update "director" SET "nationality" = '23' WHERE "id" = '2';

update "movie" SET "country" = '34' WHERE "id" = '1';
update "movie" SET "country" = '97' WHERE "id" = '2';
update "movie" SET "country" = '40' WHERE "id" = '3';
update "movie" SET "country" = '40' WHERE "id" = '4';
update "movie" SET "country" = '97' WHERE "id" = '5';