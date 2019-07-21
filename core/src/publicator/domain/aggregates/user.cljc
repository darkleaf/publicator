(ns publicator.domain.aggregates.user
  (:require
   [publicator.domain.aggregate :as agg]
   [clojure.core.match :as m]))

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

(defn- msg->tx-d [super agg msg]
  (m/match msg
    #?@(:clj [[:agg/add-attr e :user/password v]
              (conj (super agg msg)
                    [:db/add e :user/password-digest v])]) ;; todo: password hasher
    :else (super agg msg)))

(def blank
  (-> agg/blank
      (vary-meta assoc :type :agg/user)
      (agg/decorate {`agg/validate #'validate-d
                     `agg/msg->tx  #'msg->tx-d})))
