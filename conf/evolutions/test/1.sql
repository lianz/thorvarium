# --- !Ups


CREATE TABLE "user" ("id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, "nickname" VARCHAR NOT NULL, "password" VARCHAR NOT NULL);


# --- !Downs


DROP TABLE user;