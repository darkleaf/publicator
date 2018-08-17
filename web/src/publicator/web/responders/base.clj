(ns publicator.web.responders.base
  (:require
   [publicator.web.responses :as responses]
   [publicator.web.presenters.explain-data :as explain-data]
   [publicator.web.routing :as routing]))

(defmulti result->resp first)

(defmethod result->resp ::forbidden [_]
  {:status 403
   :headers {}
   :body "forbidden"})

(defmethod result->resp ::not-found [_]
  {:status 404
   :headers {}
   :body "not-found"})

(defmethod result->resp ::invalid-params [[_ explain-data]]
  (-> explain-data
      explain-data/->errors
      responses/render-errors))

(defmethod result->resp ::redirect-to-root [_]
  (responses/redirect-for-form (routing/path-for :pages/root)))
