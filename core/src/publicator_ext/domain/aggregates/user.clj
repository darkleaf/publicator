(ns publicator-ext.domain.aggregates.user
  (:require
   [publicator-ext.domain.abstractions.id-generator :as id-generator]
   [publicator-ext.domain.abstractions.instant :as instant]
   [publicator-ext.domain.abstractions.aggregate :as aggregate]
   [clojure.spec.alpha :as s]))

(def ^:const +states+ #{:active :deleted})

(s/def :user/login (s/and string? #(re-matches #"\w{3,255}" %)))
(s/def :user/password-digest ::password-hasher/encrypted)
(s/def :users/state +states+)

(s/def :entity.type/user
  (s/merge :aggregate/root
           (s/keys :req [:user/state :user/login :user/password-digest])))

(defn build [params]
  (aggregate/build (merge {:user/state :active}
                          params
                          {:aggregate/id (id-generator/generate :user)
                           :entity/type  :entity.type/user})))
