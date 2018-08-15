(ns publicator.web.controllers.user.log-out
  (:require
   [publicator.use-cases.interactors.user.log-out :as interactor]))

(defn process [_]
  [interactor/process])

(def routes
  #{[:post "/log-out" #'process :user.log-out/process]})
