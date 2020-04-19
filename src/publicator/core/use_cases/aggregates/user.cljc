(ns publicator.core.use-cases.aggregates.user
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.author :as author]
   [datascript.core :as d]))

(def states #{:active :archived})

(swap! agg/schema merge
       {:user/login           {:agg/predicate #"\w{3,255}"}
        :user/state           {:agg/predicate states}
        :user/admin?          {:agg/predicate boolean?}
        :user/author?         {:agg/predicate boolean?}
        :user/password-digest {:agg/predicate #".{1,255}"}
        :user/password        {:agg/predicate #".{8,255}"}})

(defn active? [user]
  (boolean
   (and (some? user)
        (seq (d/datoms user :eavt :root :user/state :active)))))

(defn admin? [user]
  (-> user
      (d/entity :root)
      :user/admin?))

(defn author? [user]
  (-> user
      (d/entity :root)
      :user/author?))

(defn validate [user]
  (cond-> user
    :always        (agg/validate)
    :always        (agg/required-validator {:root #{:user/login :user/state :user/password-digest}})
    (author? user) (author/validate)))
