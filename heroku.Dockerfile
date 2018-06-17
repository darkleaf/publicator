FROM clojure:tools-deps-alpine

ENV GITLIBS=/.gitlibs
ENV CLJ_CONFIG=/app/heroku-clojure

COPY . /app
WORKDIR /app/main

RUN clojure -e "(prn :install)"

CMD clojure -Astart
