(ns publicator.web.controllers.user.log-in
  (:require
   [publicator.use-cases.interactors.user.log-in :as interactor]))

(defn initial-params [req]
  [interactor/initial-params])

(defn process [{:keys [transit-params]}]
  [interactor/process transit-params])

(def routes
  #{[:get "/log-in" #'initial-params :user.log-in/initial-params]
    [:post "/log-in" #'process :user.log-in/process]})
