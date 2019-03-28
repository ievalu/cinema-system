# --- !Ups

alter table "director" add constraint "height" check ("height" > 0 and "height" < 300);
alter table "actor" add constraint "height" check ("height" > 0 and "height" < 300);

# --- !Downs

alter table "director" alter column "height" type int;
alter table "actor" alter column "height" type int;