(ns publicator.use-cases.user.log-in-form
  (:require
   [publicator.domain.aggregate :as agg]
   [darkleaf.multidecorators :as md]))

(md/decorate agg/validate :form.user/log-in
  (fn [super agg]
    (-> (super agg)
        (agg/predicate-validator 'root
          {:user/login    #"\w{3,255}"
           :user/password #".{8,255}"})
        (agg/required-validator  'root
          #{:user/login
            :user/password}))))
