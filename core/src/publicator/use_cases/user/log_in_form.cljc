(ns publicator.use-cases.user.log-in-form
  (:require
   [publicator.domain.aggregate :as agg]))

(defn- validate-d [super agg]
  (-> (super agg)
      (agg/predicate-validator 'root
                               {:user/login    #"\w{3,255}"
                                :user/password #".{8,255}"})
      (agg/required-validator  'root
                               #{:user/login
                                 :user/password})))

(def blank
  (-> agg/blank
      (vary-meta assoc :type :form.user/log-in)
      (agg/decorate {`agg/validate #'validate-d})))
