drop type  if exists "user/state";
drop type  if exists "author.achivement/kind";
drop table if exists "user";

create type "user/state" as enum ('active', 'archived');
create type "author.achivement/kind" as enum ('legend', 'star', 'old-timer');

create table "user" (
  "agg/id"               bigserial primary key,
  "user/state"           "user/state",
  "user/admin?"          boolean,
  "user/author?"         boolean,
  "user/login"           varchar(255),
  "user/password-digest" varchar(255),

  "en$db/id" integer,
  "en$author.translation/first-name" varchar(255),
  "en$author.translation/last-name"  varchar(255),

  "ru$db/id" integer,
  "ru$author.translation/first-name" varchar(255),
  "ru$author.translation/last-name"  varchar(255),

  "es#author.achivement/root" integer array,
  "vs#author.achivement/root" integer array,

  "es#author.achivement/kind" integer array,
  "vs#author.achivement/kind" "author.achivement/kind" array,

  "es#author.achivement/assigner-id" integer array,
  "vs#author.achivement/assigner-id" bigint array
);
