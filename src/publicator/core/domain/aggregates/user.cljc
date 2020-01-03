(ns publicator.core.domain.aggregates.user
  (:require
   [publicator.core.domain.aggregate :as agg]
   [darkleaf.multidecorators :as md]))

(def states #{:active :archived})
(def roles #{:regular :admin})

(md/decorate agg/validate :agg/user
  (fn [super agg]
    (-> (super agg)
        (agg/predicate-validator 'root
          {:user/login           #"\w{3,255}"
           :user/state           states
           :user/role            roles
           :user/password-digest #".{1,255}"})
        (agg/required-validator 'root
          #{:user/login
            :user/state
            :user/role
            :user/password-digest}))))

(defn active? [user]
  (and (some? user)
       (= :active (-> user agg/root :user/state))))

(defn admin? [user]
  (and (some? user)
       (= :admin  (-> user agg/root :user/role))))
