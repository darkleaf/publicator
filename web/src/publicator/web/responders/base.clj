(ns publicator.web.responders.base
  (:require
   [publicator.web.responses :as responses]
   [publicator.web.presenters.explain-data :as explain-data]
   [publicator.web.routing :as routing]))

(defmulti result->resp (fn [result interactor-args] (first result)))

(defmethod result->resp ::forbidden [_ _]
  {:status 403
   :headers {}
   :body "forbidden"})

(defmethod result->resp ::not-found [_ _]
  {:status 404
   :headers {}
   :body "not-found"})

(defmethod result->resp ::invalid-params [[_ explain-data] _]
  (-> explain-data
      explain-data/->errors
      responses/render-errors))

(defmethod result->resp ::redirect-to-root [_ _]
  (responses/redirect-for-form (routing/path-for :pages/root)))
