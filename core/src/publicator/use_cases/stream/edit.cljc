(ns publicator.use-cases.stream.edit
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.aggregates.user :as user]
   [darkleaf.effect.core :refer [eff !]]))

(def allowed-attrs #{:stream.translation/lang
                     :stream.translation/name
                     :stream.translation/stream
                     :stream/state})

(defn- check-additional-attrs [datoms]
  (eff
    (if-some [additional (->> datoms
                              (map :a)
                              (remove allowed-attrs)
                              (set)
                              (not-empty))]
      (! [:ui/show-additional-attributes-error additional]))))

(defn- find-stream [id]
  (eff
    (if-some [stream (! [:persistence/find :agg/stream id])]
      stream
      (! [:ui/show-main-screen]))))

(defn- check-validation-errors [stream]
  (eff
    (if-some [errors (-> stream agg/validate agg/errors not-empty)]
      (! [:ui/show-validation-errors errors]))))

(defn precondition [_id]
  (eff
    (let [session (! [:session/get])
          user-id (-> session :current-user-id)
          user    (! [:persistence/find :agg/user user-id])]
      (if-not (and (user/active? user)
                   (user/admin? user))
        [:ui/show-main-screen]))))

(defn initial-tx-data [id]
  (eff
    (if-some [ex-effect (! (precondition id))]
      (! ex-effect)
      (! (find-stream id)))))

(defn process [id tx-data]
  (eff
    (if-some [ex-effect (! (precondition id))]
      (! ex-effect)
      (let [stream          (! (find-stream id))
            [stream datoms] (-> stream (agg/apply-tx* tx-data))
            _               (! (check-additional-attrs datoms))
            _               (! (check-validation-errors stream))]
        (! [:persistence/save stream])
        (! [:ui/show-main-screen])))))
