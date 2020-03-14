(ns publicator.core.use-cases.stream.new
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.user :as user]
   [publicator.core.domain.aggregates.stream :as stream]
   [publicator.core.domain.languages :as langs]
   [darkleaf.effect.core :refer [with-effects effect !]]
   [darkleaf.effect.core-analogs :refer [->!]]
   [datascript.core :as d]))

(defn- fill-defaults [stream]
  (d/db-with stream
             [{:db/ident     :root
               :stream/state :active}]))

(defn- fill-id [stream]
  (with-effects
    (let [id (! (effect [:persistence.stream/next-id]))]
      (d/db-with stream [[:db/add :root :agg/id id]]))))

(defn validate-form [form]
  (-> form
      (agg/validate)
      (agg/required-validator
       {:stream.translation/_stream [:stream.translation/lang
                                     :stream.translation/name]})
      (agg/count-validator :stream.translation/lang (count langs/languages))))

(def allowed-attributes #{:stream.translation/stream
                          :stream.translation/lang
                          :stream.translation/name})

(defn precondition []
  (with-effects
    (let [session (! (effect [:session/get]))
          user-id (-> session :current-user-id)
          user    (! (effect [:persistence.user/get-by-id user-id]))]
      (if-not (and (user/active? user)
                   (user/admin? user))
        (effect [:ui.screen/show :main])))))

(defn process []
  (with-effects
    (if-some [ex-effect (! (precondition))]
      (! ex-effect)
      (loop [form (agg/allocate)]
        (let [tx-data (! (effect [:ui.form/edit form]))
              form    (-> form
                          (d/with tx-data)
                          (agg/check-report-tx-data! (comp allowed-attributes :a))
                          :db-after
                          (validate-form))]
          (if (agg/has-errors? form)
            (recur form)
            (let [stream (->! (agg/allocate)
                              (d/db-with tx-data)
                              (fill-defaults)
                              (stream/validate)
                              (agg/check-errors!)
                              (fill-id))]
              (! (effect [:persistence/create stream]))
              (! (effect [:ui.screen/show :main])))))))))
