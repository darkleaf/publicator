(ns publicator.pedestal.routes
  (:require
   [io.pedestal.http.route :as route]
   [publicator.controllers.user.register :as user.registration]))

(defn build []
  (route/expand-routes
   (-> #{}
       (into (user.registration/routes)))))
