(ns publicator.web.middlewares.responder
  (:require
   [publicator.web.responders.base :as responders.base]
   [publicator.web.responders.all]))

(defn wrap-reponder [handler]
  (fn [req]
    (let [[interactor & args] (handler req)
          result              (apply interactor args)]
      (responders.base/->resp result args))))
