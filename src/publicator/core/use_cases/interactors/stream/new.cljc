(ns publicator.core.use-cases.interactors.stream.new
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.user :as user]
   [publicator.core.domain.aggregates.stream :as stream]
   [publicator.core.domain.languages :as langs]
   [publicator.core.use-cases.services.user-session :as user-session]
   [darkleaf.effect.core :refer [with-effects effect !]]
   [darkleaf.effect.core-analogs :refer [->!]]
   [datascript.core :as d]))

(defn- fill-defaults [stream]
  (d/db-with stream
             [{:db/ident     :root
               :stream/state :active}]))

(defn validate-form [form]
  (-> form
      (agg/validate)
      (agg/required-validator
       {:stream.translation/_stream [:stream.translation/lang
                                     :stream.translation/name]})
      (agg/count-validator :stream.translation/lang (count langs/languages))
      (agg/permitted-attrs-validator #{:stream.translation/stream
                                       :stream.translation/lang
                                       :stream.translation/name})))

(defn- check-form! [form]
  (if (agg/has-errors? form)
    (effect [::->invalid-form form])
    form))

(defn- create-stream [stream]
  (effect [:persistence.stream/create stream]))

(defn precondition []
  (with-effects
    (let [user (! (user-session/user))]
      (if-not (and (user/active? user)
                   (user/admin? user))
        (effect [::->unauthorized])
        :pass))))

(defn form []
  (with-effects
    (! (! (precondition)))
    (! (effect [::->form (agg/allocate)]))))

(defn process [form]
  (with-effects
    (! (! (precondition)))
    (->! form
         (validate-form)
         (check-form!))
    (let [stream (->! form
                      (fill-defaults)
                      (stream/validate)
                      (agg/check-errors!)
                      (create-stream))]
      (! (effect [::->processed stream])))))
