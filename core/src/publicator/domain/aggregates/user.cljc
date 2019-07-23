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
    #?@(:clj [[:user/password :add e v]
              (concat (super agg msg)
                      (super agg [:user/password-digest :add e v])) ;; todo: password hasher

              [:user/password :retract e v]
              (concat (super agg msg)
                      (super agg [:user/password-digest :retract e]))

              [:user/password :retract e]
              (concat (super agg msg)
                      (super agg [:user/password-digest :retract e]))])
    :else (super agg msg)))

(def blank
  (-> agg/blank
      (vary-meta assoc :type :agg/user)
      (agg/decorate {`agg/validate #'validate-d
                     `agg/msg->tx  #'msg->tx-d})))
