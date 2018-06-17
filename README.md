# Demo

https://darkleaf-publicator2.herokuapp.com/

# Разработка в docker
+ `docker-compose run  --rm --service-ports app sh`
+ перейти в подпроект, например `cd core`
+ `clojure -Adev:repl` или `clojure -Adev:cider`

# Запуск в docker
+ `docker-compose run  --rm --service-ports app sh`
+ `cd main`
+ `clojure -Astart`
+ http://localhost:4446/
