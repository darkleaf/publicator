(ns publicator.web.middlewares.responder
  (:require
   [publicator.web.responders.base :as responders.base]
   [publicator.web.responders.all]))

(defn wrap-reponder [handler]
  (fn [req]
    (let [resp (handler req)]
      (if (vector? resp)
        (let [[interactor & args] resp
              result              (apply interactor args)]
          (responders.base/->resp result args))
        resp))))
