(ns publicator.web.responders.base)

(defmulti ->resp (fn [result interactor-args] (first result)))

(defmethod ->resp ::forbidden [_ _]
  {:status 403
   :headers {}
   :body "forbidden"})

(defmethod ->resp ::not-found [_ _]
  {:status 404
   :headers {}
   :body "not-found"})
