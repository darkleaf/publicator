(ns publicator.web.controllers.post.create
  (:require
   [publicator.use-cases.interactors.post.create :as interactor]))

(defn initial-params [req]
  [interactor/initial-params])

(defn process [{:keys [transit-params]}]
  [interactor/process transit-params])

(def routes
  #{[:get "/new-post" #'initial-params :post.create/initial-params]
    [:post "/new-post" #'process :post.create/process]})
