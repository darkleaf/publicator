(ns publicator.core.domain.aggregates.user
  (:require
   [publicator.core.domain.aggregate :as agg]
   [datascript.core :as d]
   [darkleaf.multidecorators :as md]))

(def states #{:active :archived})
(def roles #{:regular :admin})

(md/decorate agg/validate :agg/user
  (fn [super agg]
    (-> (super agg)
        (agg/required-validator {:root [:user/login
                                        :user/state
                                        :user/role
                                        :user/password-digest]})
        (agg/predicate-validator {:user/login            #"\w{3,255}"
                                  :user/state            states
                                  :user/role             roles
                                  :user/password-digest #".{1,255}"}))))

(defn active? [user]
  (and (some? user)
       (seq (d/datoms user :eavt :root :user/state :active))))

(defn admin? [user]
  (and (some? user)
       (seq (d/datoms user :eavt :root :user/role :admin))))
