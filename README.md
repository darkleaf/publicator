[![CircleCI](https://circleci.com/gh/darkleaf/publicator/tree/master.svg?style=svg)](https://circleci.com/gh/darkleaf/publicator/tree/master)

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

# Heroku deploy

+ `docker-compose run  --rm --service-ports app sh`
+ `cd main`
+ `clojure -Auberjar`, выполняется долго из-за docker
+ выйти в из docker
+ `cd main`
+ `heroku deploy:jar main.jar`
