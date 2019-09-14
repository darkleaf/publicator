(ns publicator.domain.aggregates.user
  (:require
   [publicator.domain.aggregate :as agg]
   [darkleaf.multidecorators :as md]))

(def states #{:active :archived})

(defn validate-decorator [super agg]
  (-> (super agg)
      (agg/predicate-validator 'root
                               {:user/login    #"\w{3,255}"
                                :user/password #".{8,255}"
                                :user/state    states})
      (agg/required-validator  'root
                               #{:user/login
                                 :user/state})
      #?(:clj (agg/predicate-validator 'root  {:user/password-digest #".{1,255}"}))
      #?(:clj (agg/required-validator  'root #{:user/password-digest}))))
(md/decorate agg/validate :agg/user #'validate-decorator)

(derive :agg/new-user :agg/user)

(defn new-validate-decorator [super agg]
  (-> (super agg)
      (agg/required-validator 'root
                              #{:user/password})))
(md/decorate agg/validate :agg/new-user #'new-validate-decorator)
