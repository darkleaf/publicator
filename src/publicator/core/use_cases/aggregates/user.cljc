(ns publicator.core.use-cases.aggregates.user
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.author :as author]
   [datascript.core :as d]))

(def states #{:active :archived})
(def roles #{:author :admin})

(swap! agg/schema merge
       {:user/login           {:agg/predicate #"\w{3,255}"}
        :user/state           {:agg/predicate states}
        :user/role            {:db/cardinality :db.cardinality/many :agg/predicate roles}
        :user/password-digest {:agg/predicate #".{1,255}"}
        :user/password        {:agg/predicate #".{8,255}"}})

(defn active? [user]
  (boolean
   (and (some? user)
        (seq (d/datoms user :eavt :root :user/state :active)))))

(defn admin? [user]
  (boolean
   (and (some? user)
        (seq (d/datoms user :eavt :root :user/role :admin)))))

(defn author? [user]
  (boolean
   (and (some? user)
        (seq (d/datoms user :eavt :root :user/role :author)))))

(defn validate [user]
  (cond-> user
    :always        (agg/validate)
    :always        (agg/required-validator {:root #{:user/login :user/state :user/password-digest}})
    (author? user) (author/validate)))
