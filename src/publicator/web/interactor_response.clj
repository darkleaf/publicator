(ns publicator.web.interactor-response)

(defmulti handle :type)

(defmethod handle ::forbidden [resp]
  {:status 403
   :headers {}
   :body "forbidden"})

(defmethod handle ::not-found [resp]
  {:status 404
   :headers {}
   :body "not-found"})
