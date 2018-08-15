(ns publicator.web.middlewares.responder
  (:require
   [publicator.web.responders.base :as responders.base]

   [publicator.web.responders.pages.root]
   [publicator.web.responders.user.log-in]
   [publicator.web.responders.user.log-out]
   [publicator.web.responders.user.register]
   [publicator.web.responders.post.list]
   [publicator.web.responders.post.show]
   [publicator.web.responders.post.create]
   [publicator.web.responders.post.update]))

(defn wrap-reponder [handler]
  (fn [req]
    (let [resp (handler req)]
      (if (vector? resp)
        (let [[interactor & args] resp
              result              (apply interactor args)]
          (responders.base/->resp result args))
        resp))))
