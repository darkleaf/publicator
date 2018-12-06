-- :name- post-get-list :? :n
SELECT "post".*,
       "user"."id"        AS "publicator.domain.aggregates.user/id",
       "user"."full-name" AS "publicator.domain.aggregates.user/full-name"
FROM "post"
JOIN "user" ON "user"."posts-ids" @> ARRAY["post"."id"]

-- :name- post-get-by-id :? :1
SELECT "post".*,
       "user"."id"        AS "publicator.domain.aggregates.user/id",
       "user"."full-name" AS "publicator.domain.aggregates.user/full-name"
FROM "post"
JOIN "user" ON "user"."posts-ids" @> ARRAY["post"."id"]
WHERE "post"."id" = :id
