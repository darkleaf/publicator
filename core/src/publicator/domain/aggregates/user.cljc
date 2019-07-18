(ns publicator.domain.aggregates.user
  (:require
   [publicator.domain.aggregate :as agg]))

(def states #{:active :archived})

(defn- validate-d [super agg]
  (-> (super agg)
      (agg/predicate-validator 'root
                               {:user/login    #"\w{3,255}"
                                :user/password #".{8,255}"
                                :user/state    states})
      (agg/required-validator  'root
                               #{:user/login
                                 :user/state})
      (agg/required-validator  'blank
                               #{:user/password})
      #?(:clj (agg/predicate-validator 'root  {:user/password-digest #".{1,255}"}))
      #?(:clj (agg/required-validator  'root #{:user/password-digest}))))

(def blank
  (-> agg/blank
      (vary-meta assoc :type :agg/user)
      (agg/decorate {`agg/validate #'validate-d})))
