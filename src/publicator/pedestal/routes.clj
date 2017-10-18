(ns publicator.pedestal.routes
  (:require
   [io.pedestal.http.route :as route]
   [publicator.web.user.register.controller :as user.register]
   [publicator.web.pages.root.controller :as pages.root]))

(defn build []
  (route/expand-routes
   (-> #{}
       (into (pages.root/routes))
       (into (user.register/routes)))))
