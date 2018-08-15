(ns publicator.web.controllers.post.show
  (:require
   [publicator.use-cases.interactors.post.show :as interactor]))

(defn process [{:keys [route-params]}]
  (let [id (-> route-params :id Integer.)]
    [interactor/process id]))

(def routes
  #{[:get "/posts/:id{\\d+}" #'process :post.show/process]})
