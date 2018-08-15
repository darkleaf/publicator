(ns publicator.web.controllers.post.list
  (:require
   [publicator.use-cases.interactors.post.list :as interactor]))

(defn process [req]
  [interactor/process])

(def routes
  #{[:get "/posts" #'process :post.list/process]})
