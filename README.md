# Разработка в docker
+ `docker-compose run  --rm --service-ports app sh`
+ перейти в подпроект, например `cd core`
+ `clojure -Adev:repl` или `clojure -Adev:cider`

# Разработка на хосте

Положите в `$HOME/.clojure/` файл подобный `docker-clojure/deps.edn` с вашими настройками cider/repl.
Подробнее: https://clojure.org/reference/deps_and_cli#_directories.
