drop table if exists "user";
drop type  if exists "user/state";
drop type  if exists "author.achivement/kind";

drop table if exists "publication";
drop type  if exists "publication/state";
drop type  if exists "publication/type";
drop type  if exists "publication.translation/state";

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

-- TODO: foreign keys via additional table, может быть даже на триггерах


create type "publication/state" as enum ('active', 'archived');
create type "publication/type" as enum ('article', 'gallery');
create type "publication.translation/state" as enum ('draft', 'published');

create table "publication" (
  "agg/id"                 bigserial primary key,
  "publication/state"      "publication/state",
  "publication/type"       "publication/type",
  "publication/author-id"  bigint,
  "publication/related-id" bigint array,

  "article/image-url" varchar(255),
  "gallery/image-url" varchar(255) array,

  "en$publication.translation/state"        "publication.translation/state",
  "en$publication.translation/title"        varchar(255),
  "en$publication.translation/summary"      varchar(255),
  "en$publication.translation/published-at" timestamptz,
  "en$publication.translation/tag"          varchar(255) array,
  "en$article.translation/content"          text,

  "ru$publication.translation/state"        "publication.translation/state",
  "ru$publication.translation/title"        varchar(255),
  "ru$publication.translation/summary"      varchar(255),
  "ru$publication.translation/published-at" timestamptz,
  "ru$publication.translation/tag"          varchar(255) array,
  "ru$article.translation/content"          text
);

-- идентификаторы отрицательные, чтобы не заморачиваться с sequences

insert into "user" values (
  -1, 'active', true, true, 'admin', 'digest',
  2, 'John', 'Doe',
  3, 'Иван', 'Иванов',

  '{4}', '{1}',
  '{4}', '{"star"}',
  '{4}', '{-1}' -- сам себе ачивку виписал, это некорректно
);

insert into "publication" values (
  -1, 'active', 'article', -1, '{-1}',
  'cat.png', null,
  'published', 'Funny cat', 'summary', '2020-01-01 00:00:00Z', '{cat}', 'text',
  'published', 'Забавный кот', 'описание', '2020-01-01 00:00:00Z', '{кот}', 'текст'
), (
  -2, 'active', 'gallery', -1, '{-2}',
  null, '{cat-1.png,cat-2.png}',
  'published', 'Funny cat', 'summary', '2020-01-01 00:00:00Z', '{cat}', null,
  'published', 'Забавный кот', 'описание', '2020-01-01 00:00:00Z', '{кот}', null
);
