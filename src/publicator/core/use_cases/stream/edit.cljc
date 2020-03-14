(ns publicator.core.use-cases.stream.edit
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.user :as user]
   [publicator.core.domain.aggregates.stream :as stream]
   [darkleaf.effect.core :refer [with-effects effect !]]
   [darkleaf.effect.core-analogs :refer [->!]]
   [datascript.core :as d]))

;; смену статуса сделать отдельным юзкейсом
;; тут нужно посмотреть на роли и сделать смену тут

(defn- find-stream [id]
  (with-effects
    (if-some [stream (! (effect [:persistence.stream/get-by-id id]))]
      stream
      (! (effect [:ui/show-main-screen])))))

(defn stream->form [stream]
  stream)

(defn allowed-attrs []
  (with-effects
    (let [session (! (effect [:session/get]))
          user-id (-> session :current-user-id)
          user    (! (effect [:persistence.user/get-by-id user-id]))]
      (cond-> #{:stream.translation/stream
                :stream.translation/lang
                :stream.translation/name}
        (user/admin? user) (conj :stream/state)))))

(defn validate-form [form]
  (stream/validate form))

(defn precondition [_id]
  (with-effects
    (let [session (! (effect [:session/get]))
          user-id (-> session :current-user-id)
          user    (! (effect [:persistence.user/get-by-id user-id]))]
      (if-not (and (user/active? user)
                   (user/admin? user))
        (effect [:ui/show-main-screen])))))

(defn process [id]
  (with-effects
    (if-some [ex-effect (! (precondition id))]
      (! ex-effect)
      (let [stream            (! (find-stream id))
            the-allowed-attrs (! (allowed-attrs))]
        (loop [form (stream->form stream)]
          ;; тут можно взять блокироку на редактирование
          ;; если она не берется, то показать сообщение
          (let [tx-data (! (effect [:ui.form/edit form]))
                form    (-> form
                            (d/with tx-data)
                            (agg/check-report-tx-data! (comp the-allowed-attrs :a))
                            :db-after
                            (validate-form))]
            (if (agg/has-errors? form)
              (recur form)
              (let [stream (-> stream
                               (d/db-with tx-data)
                               (stream/validate)
                               (agg/check-errors!))]
                (! (effect [:persistence.stream/update stream]))
                (! (effect [:ui/show-main-screen]))))))))))
