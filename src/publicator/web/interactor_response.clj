(ns publicator.web.interactor-response
  (:require
   [form-ujs.spec]
   [publicator.transit :as transit]
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
  {:status  422
   :headers {"Content-Type" "application/transit+json"}
   :body    (->> resp
                 :explain-data
                 (form-ujs.spec/errors problem-presenter/present)
                 (transit/write-str))})
