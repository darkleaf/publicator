CREATE TABLE "user" (
  "id" bigint PRIMARY KEY,
  "login" varchar(255) UNIQUE,
  "full-name" varchar(255),
  "password-digest" text
);
