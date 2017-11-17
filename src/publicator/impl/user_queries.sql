-- :name- user-get-by-login :? :1
SELECT * FROM "user" WHERE login = :login LIMIT 1
