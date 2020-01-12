(ns publicator.core.use-cases.stream.new
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.user :as user]
   [darkleaf.effect.core :refer [with-effects effect !]]
   [darkleaf.effect.core-analogs :refer [->!]]))

(defn- fill-defaults [stream]
  (agg/apply-tx stream
                [{:db/ident     :root
                  :stream/state :active}]))

(defn- fill-id [stream]
  (with-effects
    (let [id (! (effect [:persistence/next-id :stream]))]
      (agg/apply-tx stream [[:db/add :root :agg/id id]]))))

(defn precondition []
  (with-effects
    (let [session (! (effect [:session/get]))
          user-id (-> session :current-user-id)
          user    (! (effect [:persistence/find :agg/user user-id]))]
      (if-not (and (user/active? user)
                   (user/admin? user))
        (effect [:ui.screen/show :main])))))

(defn process []
  (with-effects
    (if-some [ex-effect (! (precondition))]
      (! ex-effect)
      (loop [form (agg/allocate :agg.stream/base)]
        (let [tx-data (! (effect [:ui.form/edit form]))
              form    (->! form
                           (agg/apply-tx! tx-data)
                           (agg/validate))]
          (if (agg/has-errors? form)
            (recur form)
            (let [stream (->! (agg/allocate :agg/stream)
                              (agg/apply-tx tx-data)
                              (fill-defaults)
                              (agg/validate)
                              (agg/check-errors)
                              (fill-id))]
              (! (effect [:persistence/create stream]))
              (! (effect [:ui.screen/show :main])))))))))
