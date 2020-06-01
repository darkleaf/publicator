create type "user/state" as enum ('active', 'archived');

create table "user" (
  "agg/id"               serial primary key,
  "user/state"           "user/state",
  "user/admin?"          boolean,
  "user/author?"         boolean,
  "user/login"           varchar(255),
  "user/password-digest" varchar(255),

  "author.translation"            integer array,
  "author.translation/lang"       language array,
  "author.translation/first-name" varchar(255) array,
  "author.translation/last-name"  varchar(255) array
);
