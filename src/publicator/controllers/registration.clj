(ns publicator.controllers.registration
  (:require
   [publicator.views.registration.new :as views.new]))

(defn new [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (views.new/render nil)})

(defn routes []
  #{["/registration/new" :get new :route-name :new-registration]})
