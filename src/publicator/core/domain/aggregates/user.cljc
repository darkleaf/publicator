(ns publicator.core.domain.aggregates.user
  (:require
   [publicator.core.domain.aggregate :as agg]
   [datascript.core :as d]))

(def states #{:active :archived})
(def roles #{:regular :admin})

(swap! agg/schema merge
       {:user/login           {:agg/predicate #"\w{3,255}"}
        :user/state           {:agg/predicate states}
        :user/role            {:agg/predicate roles}
        :user/password-digest {:agg/predicate #".{1,255}"}})

(defn validate [agg]
  (-> agg
      (agg/validate)
      (agg/required-validator
       {:root [:user/login
               :user/state
               :user/role
               :user/password-digest]})))

(defn active? [user]
  (and (some? user)
       (seq (d/datoms user :eavt :root :user/state :active))))

(defn admin? [user]
  (and (some? user)
       (seq (d/datoms user :eavt :root :user/role :admin))))
