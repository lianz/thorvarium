# --- !Ups


CREATE TABLE "person" ("id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE,
  "name" VARCHAR NOT NULL,
  "life" INTEGER NOT NULL,
  "speed" INTEGER NOT NULL,
  "size" INTEGER NOT NULL,
  "distance" INTEGER NOT NULL);


# --- !Downs


DROP TABLE person;