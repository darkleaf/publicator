(ns publicator.domain.user
  (:require
   [publicator.domain.abstractions.hasher :as hasher]
   [publicator.domain.abstractions.id-generator :as id-generator]
   [publicator.domain.abstractions.aggregate :as aggregate]
   [clojure.spec.alpha :as s]))

(s/def ::id ::id-generator/id)
(s/def ::login (s/and string? #(re-matches #"\w{3,255}" %)))
(s/def ::full-name (s/and string? #(re-matches #".{2,255}" %)))
(s/def ::password (s/and string? #(re-matches #".{8,255}" %)))
(s/def ::password-digest ::hasher/encrypted)

(s/def ::attrs (s/keys :req-un [::id ::login ::full-name ::password-digest]))

(defrecord User [id login full-name password-digest]
  aggregate/Aggregate
  (id [_] id)
  (spec [_] ::attrs))

(s/def ::build-params (s/keys :req-un [::login ::full-name ::password]))

(defn build [{:keys [login full-name password] :as params}]
  (s/assert ::build-params params)
  (let [id              (id-generator/generate)
        password-digest (hasher/derive password)]
    (map->User {:id              id
                :login           login
                :full-name       full-name
                :password-digest password-digest})))

(defn authenticated? [{:keys [password-digest]} password]
  (hasher/check password password-digest))
