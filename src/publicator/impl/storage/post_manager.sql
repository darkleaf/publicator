-- :name- post-insert :!
INSERT INTO "post" VALUES :tuple*:vals;

-- :name- post-select :? :*
SELECT *, xmin AS version FROM "post" WHERE id IN (:v*:ids)

-- :name- post-delete :!
DELETE FROM "post" WHERE id IN (:v*:ids)

-- :name- post-locks :? :*
SELECT id, xmin AS version FROM "post" WHERE id IN (:v*:ids) FOR UPDATE
