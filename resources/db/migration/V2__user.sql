create type "user/state" as enum ('active', 'archived');

create table "user" (
  "r:agg/id"               serial primary key,
  "r:user/state"           "user/state",
  "r:user/admin?"          boolean,
  "r:user/author?"         boolean,
  "r:user/login"           varchar(255),
  "r:user/password-digest" varchar(255),

  "e:author.translation/author"     integer array,
  "v:author.translation/author"     integer array,
  "e:author.translation/lang"       integer array,
  "v:author.translation/lang"       language array,
  "e:author.translation/first-name" integer array,
  "v:author.translation/first-name" varchar(255) array,
  "e:author.translation/last-name"  integer array,
  "v:author.translation/last-name"  varchar(255) array
);
