(ns publicator.core.use-cases.aggregates.user
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.author :as author]
   [datascript.core :as d]))

(def states #{:active :archived})

(swap! agg/schema merge
       {:user/login           {:agg/predicate #"\w{3,255}"}
        :user/state           {:agg/predicate states}
        ;; admin? и author? сделаны отдельными полями для разграничения доступа по полям
        :user/admin?          {:agg/predicate boolean?}
        :user/author?         {:agg/predicate boolean?}
        :user/password-digest {:agg/predicate #".{1,255}"}
        :user/password        {:agg/predicate #".{8,255}"}})

(defn active? [user]
  (agg/include? user :root :user/state :active))

(defn admin? [user]
  (agg/include? user :root :user/admin? true))

(defn author? [user]
  (agg/include? user :root :user/author? true))

(defn validate [user]
  (cond-> user
    :always        (agg/validate)
    :always        (agg/required-attrs-validator
                    {:root #{:user/login :user/state :user/password-digest}})
    (author? user) (author/validate)))
