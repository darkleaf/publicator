(ns publicator.pedestal.routes
  (:require
   [io.pedestal.http.route :as route]
   [publicator.web.user.register.controller :as user.register]
   [publicator.web.user.log-in.controller :as user.log-in]
   [publicator.web.pages.root.controller :as pages.root]))

(defn build []
  (route/expand-routes
   (reduce into [(pages.root/routes)
                 (user.register/routes)
                 (user.log-in/routes)])))
