(ns publicator.interactors.projections.post-list
  (:require
   [publicator.domain.user :as user]
   [publicator.domain.post :as post]
   [clojure.spec.alpha :as s]))

(s/def ::author (s/keys :req-un [::user/full-name]))
(s/def ::item (s/keys :req-un [::post/id ::post/title ::author]))
(s/def ::list (s/coll-of ::item))
