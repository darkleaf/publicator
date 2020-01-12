(ns publicator.core.use-cases.stream.list
  (:require
   [publicator.core.domain.aggregate :as agg]
   [publicator.core.use-cases.stream.edit :as edit]
   [darkleaf.effect.core :refer [with-effects effect !]]
   [darkleaf.effect.core-analogs :as e.ca]))

(defn- stream->view [stream]
  (with-effects
    (let [session        (! (effect [:session/get]))
          lang           (get session :lang :ru)
          id             (-> stream agg/root :agg/id)
          edit-ex-effect (! (edit/precondition id))]
      {:agg/id           id
       :ui/can-edit?     (nil? edit-ex-effect)
       :stream.view/name (agg/q stream '[:find ?name .
                                         :in ?lang
                                         :where
                                         [?t :stream.translation/stream :root]
                                         [?t :stream.translation/lang ?lang]
                                         [?t :stream.translation/name ?name]]
                                lang)})))

(defn precondition [])

(defn process []
  (with-effects
    (if-some [ex-effect (! (precondition))]
      (! ex-effect)
      (let [streams (! (effect [:persistence/active-streams]))
            ;; здесь можно вызвать (! (effect [:persistence.hint/preload...]))
            ;; для устранения возможного N+1
            views   (! (e.ca/mapv! stream->view streams))]
        (! (effect [:ui.screen/show :streams views]))))))
