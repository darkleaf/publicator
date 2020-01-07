(ns publicator.core.use-cases.stream.list)
;;   (:require
;;    [publicator.core.domain.aggregate :as agg]
;;    [publicator.core.use-cases.stream.edit :as edit]
;;    [darkleaf.effect.core :refer [eff !] :as e]))

;; (defn- stream->view [stream]
;;   (eff
;;     (let [session        (! [:session/get])
;;           lang           (get session :lang :ru)
;;           id             (-> stream agg/root :agg/id)
;;           edit-ex-effect (! (edit/precondition id))]
;;       {:agg/id           id
;;        :ui/can-edit?     (nil? edit-ex-effect)
;;        :stream.view/name (agg/q stream '[:find ?name .
;;                                          :in ?lang
;;                                          :where
;;                                          [?t :stream.translation/stream :root]
;;                                          [?t :stream.translation/lang ?lang]
;;                                          [?t :stream.translation/name ?name]]
;;                                 lang)})))

;; (defn precondition [])

;; (defn process []
;;   (eff
;;     (if-some [ex-effect (! (precondition))]
;;       (! ex-effect)
;;       (let [streams (! [:persistence/active-streams])
;;             ;; здесь можно вызвать (! [:persistence.hint/preload...])
;;             ;; для устранения возможного N+1
;;             views   (! (e/mapv stream->view streams))]
;;         (! [:ui/render-streams views])))))
