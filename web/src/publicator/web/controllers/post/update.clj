(ns publicator.web.controllers.post.update
  (:require
   [publicator.use-cases.interactors.post.update :as interactor]))

(defn- req->id [req]
  (-> req
      :route-params
      :id
      Integer.))

(defn initial-params [req]
  (let [id (req->id req)]
    [interactor/initial-params id]))

(defn process [{:keys [transit-params] :as req}]
  (let [id (req->id req)]
    [interactor/process id transit-params]))

(def routes
  #{[:get "/posts/:id{\\d+}/edit" #'initial-params :post.update/initial-params]
    [:post "/posts/:id{\\d+}/edit" #'process :post.update/process]})
