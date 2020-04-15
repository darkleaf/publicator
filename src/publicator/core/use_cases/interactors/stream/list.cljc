(ns publicator.core.use-cases.interactors.stream.list
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.use-cases.interactors.stream.edit :as edit]
   [darkleaf.effect.core :refer [with-effects effect !]]
   [darkleaf.effect.core-analogs :as e.ca]
   [datascript.core :as d]))

(defn- stream->view [stream]
  (with-effects
    (let [session        (! (effect [:session/get]))
          lang           (get session :lang :ru)
          id             (d/q '[:find ?v . :where [:root :agg/id ?v]] stream)
          edit-ex-effect (! (edit/precondition id))]
      {:agg/id           id
       :ui/can-edit?     (nil? edit-ex-effect)
       :stream.view/name (d/q '[:find ?name .
                                :in $ ?lang
                                :where
                                [?t :stream.translation/stream :root]
                                [?t :stream.translation/lang ?lang]
                                [?t :stream.translation/name ?name]]
                              stream lang)})))

(defn precondition [])

(defn process []
  (with-effects
    (if-some [ex-effect (! (precondition))]
      (! ex-effect)
      (let [streams (! (effect [:persistence.stream/active]))
            ;; здесь можно вызвать (! (effect [:persistence.hint/preload...]))
            ;; для устранения возможного N+1
            views   (! (e.ca/mapv! stream->view streams))]
        (! (effect [:ui.screen.streams/show views]))))))
