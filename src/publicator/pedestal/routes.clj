(ns publicator.pedestal.routes
  (:require
   [io.pedestal.http.route :as route]
   [publicator.web.user.register.controller :as user.register]))

(defn build []
  (route/expand-routes
   (-> #{}
       (into (user.register/routes)))))
