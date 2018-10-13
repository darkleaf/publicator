-- :name- create-test-entity-table :! :raw
CREATE TABLE "test-entity" (
  "id" bigint PRIMARY KEY,
  "counter" integer
);

-- :name- drop-test-entity-table :! :raw
DROP TABLE IF EXISTS "test-entity"

-- :name- test-entity-insert :!
INSERT INTO "test-entity" VALUES :tuple*:vals;

-- :name- test-entity-select :? :*
SELECT *, xmin AS version FROM "test-entity" WHERE id IN (:v*:ids)

-- :name- test-entity-delete :!
DELETE FROM "test-entity" WHERE id IN (:v*:ids)

-- :name- test-entity-locks :? :*
SELECT id, xmin AS version FROM "test-entity" WHERE id IN (:v*:ids) FOR UPDATE
