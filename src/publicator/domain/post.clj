(ns publicator.domain.post
  (:require
   [publicator.domain.abstractions.id-generator :as id-generator]
   [publicator.domain.abstractions.aggregate :as aggregate]
   [clojure.spec.alpha :as s]))

(s/def ::id ::id-generator/id)
(s/def ::title (s/and string? #(re-matches #".{1,255}" %)))
(s/def ::content string?)
(s/def ::author-id ::id-generator/id)

(s/def ::attrs (s/keys :req-un [::id ::author-id ::title ::content]))

(defrecord Post [id author-id title]
  aggregate/Aggregate
  (spec [_] ::attrs))

(s/def ::build-params (s/keys :req-un [::title ::content ::author-id]))

(defn build [params]
  {:pre [(s/assert ::build-params params)]}
  (let [id (id-generator/generate)]
    (map->Post (merge params
                      {:id id}))))
