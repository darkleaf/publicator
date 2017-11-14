CREATE TABLE "user" (
  "id" bigint PRIMARY KEY,
  "login" varchar(255),
  "full-name" varchar(255),
  "password-digest" text,
  "posts-count" integer
);
