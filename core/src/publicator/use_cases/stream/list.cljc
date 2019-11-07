(ns publicator.use-cases.stream.list
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.use-cases.stream.edit :as edit]
   [darkleaf.effect.core :refer [eff !]]))

(defn- stream->view [stream]
  (eff
    (let [session (! [:session/get])
          lang (get session :lang :ru)]
      {:agg/id           (agg/q stream '[:find ?id .
                                         :where
                                         [:root :agg/id ?id]])
       :stream.view/name (agg/q stream '[:find ?name .
                                         :in ?lang
                                         :where
                                         [?t :stream.translation/stream :root]
                                         [?t :stream.translation/lang ?lang]
                                         [?t :stream.translation/name ?name]]
                                lang)})))

(defn precondition [])

(defn process []
  (eff
    (if-some [ex-effect (! (precondition))]
      (! ex-effect)
      (loop [[stream streams] (! [:persistence/active-streams])
             acc              []]
        (if (nil? stream)
          (! [:ui/render-streams acc])
          (let [id        (-> stream agg/root :agg/id)
                view      (! (stream->view stream))
                ex-effect (! (edit/precondition id))
                view      (assoc view :ui/can-edit? (nil? ex-effect))]
            (recur (conj acc view) streams)))))))
