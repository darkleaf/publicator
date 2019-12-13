(ns publicator.core.use-cases.stream.edit
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.user :as user]
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

(defn process [id]
  (eff
    (if-some [ex-effect (! (precondition id))]
      (! ex-effect)
      (let [stream          (! (find-stream id))
            ;; тут можно взять блокироку на редактирование
            ;; если она не берется, то показать сообщение
            tx-data         (! [:ui/edit stream])
            [stream datoms] (-> stream (agg/apply-tx* tx-data))
            _               (! (check-additional-attrs datoms))
            _               (! (check-validation-errors stream))]
        (! [:persistence/save stream])
        (! [:ui/show-main-screen])))))
