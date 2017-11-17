-- :name- post-get-list :? :n
SELECT post.id,
       post.title,
       post."author-id",
       "user"."full-name" AS "author-full-name"
FROM post
JOIN "user" ON "user".id = post."author-id"
