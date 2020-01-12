(ns publicator.core.use-cases.stream.edit
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.user :as user]
   [darkleaf.effect.core :refer [with-effects effect !]]
   [darkleaf.effect.core-analogs :refer [->!]]))

;; смену статуса сделать отдельным юзкейсом


(defn- find-stream [id]
  (with-effects
    (if-some [stream (! (effect [:persistence/find :agg/stream id]))]
      stream
      (! (effect [:ui/show-main-screen])))))

(defn precondition [_id]
  (with-effects
    (let [session (! (effect [:session/get]))
          user-id (-> session :current-user-id)
          user    (! (effect [:persistence/find :agg/user user-id]))]
      (if-not (and (user/active? user)
                   (user/admin? user))
        (effect [:ui/show-main-screen])))))

(defn process [id]
  (with-effects
    (if-some [ex-effect (! (precondition id))]
      (! ex-effect)
      (let [stream (! (find-stream id))]
        (loop [form (agg/becomes stream :agg.stream/base)]
          ;; тут можно взять блокироку на редактирование
          ;; если она не берется, то показать сообщение
          (let [tx-data (! (effect [:ui.form/edit form]))
                form (-> form
                         (agg/apply-tx! tx-data)
                         (agg/validate))]
            (if (agg/has-errors? form)
              (recur form)
              (let [stream (->! stream
                                (agg/apply-tx tx-data)
                                (agg/validate)
                                (agg/check-errors))]
                (! (effect [:persistence/update stream]))
                (! (effect [:ui/show-main-screen]))))))))))
