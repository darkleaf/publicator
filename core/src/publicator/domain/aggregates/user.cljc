(ns publicator.domain.aggregates.user
  (:require
   [publicator.domain.aggregate :as agg]
   [darkleaf.multidecorators :as md]))

(def states #{:active :archived})
(def roles #{:regular :admin})

(md/decorate agg/validate :agg/user
  (fn [super agg]
    (-> (super agg)
        (agg/predicate-validator 'root
          {:user/login    #"\w{3,255}"
           :user/password #".{8,255}"
           :user/state    states
           :user/role     roles})
        (agg/required-validator 'root
          #{:user/login
            :user/state
            :user/role})
        #?(:clj (agg/predicate-validator 'root  {:user/password-digest #".{1,255}"}))
        #?(:clj (agg/required-validator  'root #{:user/password-digest})))))

(derive :agg/new-user :agg/user)

(md/decorate agg/validate :agg/new-user
  (fn [super agg]
    (-> (super agg)
        (agg/required-validator 'root
          #{:user/password}))))

(defn active? [user]
  (and (some? user)
       (= :active (-> user agg/root :user/state))))

(defn admin? [user]
  (and (some? user)
       (= :admin  (-> user agg/root :user/role))))
