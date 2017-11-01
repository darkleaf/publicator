(ns publicator.web.interactor-response
  (:require
   [form-ujs.spec.errors]
   [form-ujs.ring]
   [publicator.web.problem-presenter :as problem-presenter]))

(defmulti handle :type)

(defmethod handle ::forbidden [resp]
  {:status 403
   :headers {}
   :body "forbidden"})

(defmethod handle ::not-found [resp]
  {:status 404
   :headers {}
   :body "not-found"})

(defmethod handle ::invalid-params [resp]
  (form-ujs.ring/failure-response
   (->> resp
        :explain-data
        (form-ujs.spec.errors/errors problem-presenter/present))))
