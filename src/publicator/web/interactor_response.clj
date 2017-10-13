(ns publicator.web.interactor-response)

(defmulti handle :type)

(defmethod handle ::forbidden [resp]
  {:status 403
   :headers {}
   :body "forbidden"})

;; not-found
