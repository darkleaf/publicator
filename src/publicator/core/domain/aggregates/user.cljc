(ns publicator.core.domain.aggregates.user
  (:require
   [publicator.core.domain.aggregate :as agg]
   [darkleaf.multidecorators :as md]))

(def states #{:active :archived})
(def roles #{:regular :admin})

(md/decorate agg/validate :agg.user/public
  (fn [super agg]
    (-> (super agg)
        (agg/predicate-validator 'root
          {:user/login    #"\w{3,255}"})
        (agg/required-validator 'root
          #{:user/login}))))

(md/decorate agg/allowed-attribute? :agg.user/public
  (fn [super type attr]
    (or (super type attr)
        (#{:user/login} attr))))

(md/decorate agg/validate :agg.user/protected
  (fn [super agg]
    (-> (super agg)
        (agg/predicate-validator 'root
          {:user/state states
           :user/role  roles})
        (agg/required-validator 'root
           #{:user/state
             :user/role}))))

(md/decorate agg/allowed-attribute? :agg.user/protected
  (fn [super type attr]
    (or (super type attr)
        (#{:user/state
           :user/role} attr))))

(md/decorate agg/validate :agg.user/private
  (fn [super agg]
    (-> (super agg)
        (agg/predicate-validator 'root
          {:user/password-digest #".{1,255}"})
        (agg/required-validator 'root
          #{:user/password-digest}))))

(md/decorate agg/allowed-attribute? :agg.user/private
  (fn [super type attr]
    (or (super type attr)
        (#{:user/password-digest} attr))))

(derive :agg/user :agg/persisting)
(derive :agg/user :agg.user/public)
(derive :agg/user :agg.user/protected)
(derive :agg/user :agg.user/private)

(defn active? [user]
  (and (some? user)
       (= :active (-> user agg/root :user/state))))

(defn admin? [user]
  (and (some? user)
       (= :admin  (-> user agg/root :user/role))))
