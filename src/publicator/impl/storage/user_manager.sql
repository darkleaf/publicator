-- :name- user-insert :!
INSERT INTO "user" VALUES :tuple*:vals;

-- :name- user-select :? :*
SELECT *, xmin AS version FROM "user" WHERE id IN (:v*:ids)

-- :name- user-delete :!
DELETE FROM "user" WHERE id IN (:v*:ids)

-- :name- user-locks :? :*
SELECT id, xmin AS version FROM "user" WHERE id IN (:v*:ids) FOR UPDATE
