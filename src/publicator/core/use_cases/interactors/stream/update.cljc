(ns publicator.core.use-cases.interactors.stream.update
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.domain.aggregates.user :as user]
   [publicator.core.domain.aggregates.stream :as stream]
   [publicator.core.use-cases.services.user-session :as user-session]
   [publicator.core.use-cases.services.form :as form]
   [darkleaf.effect.core :refer [with-effects effect !]]
   [darkleaf.effect.core-analogs :refer [->!]]
   [datascript.core :as d]
   [clojure.data :as data]))

(defn- find-stream [id]
  (with-effects
    (if-some [stream (! (effect [:persistence.stream/get-by-id id]))]
      stream
      (! (effect [::->stream-not-found])))))

(defn ->readable-attr? []
  #{:db/ident
    :agg/id
    :stream/state
    :stream.translation/stream
    :stream.translation/lang
    :stream.translation/name})

(defn ->updatable-attr? []
  ;; статус может только админ менять
  #{#_:stream/state
    :stream.translation/stream
    :stream.translation/lang
    :stream.translation/name})

(defn validate-form [form]
  (with-effects
    (-> form
        (stream/validate)
        (agg/required-validator {:root [:agg/id]})
        (agg/permitted-attrs-validator (! (->readable-attr?))))))

(defn- update-stream [stream]
  (effect [:persistence.stream/update stream]))

(defn precondition [stream]
  (with-effects
    (let [user (! (user-session/user))]
      (if (and (user/active? user)
               (user/admin? user)) ;;
        :pass
        (effect [::->unauthorized])))))

(defn form [id]
  (with-effects
    (let [stream (! (find-stream id))
          _      (! (! (precondition stream)))
          form   (form/agg->form stream (! (->readable-attr?)))]
      (! (effect [::->form form])))))

(defn process [form]
  (with-effects
    (->! form
         (validate-form)
         (form/check-errors))
    (let [{:keys [agg/id]} (d/entity form :root)
          stream           (! (find-stream id))
          _                (! (! (precondition stream)))
          stream           (->! stream
                                (form/apply-form! form (! (->updatable-attr?)))
                                (update-stream))]
      (! (effect [::->processed stream])))))
