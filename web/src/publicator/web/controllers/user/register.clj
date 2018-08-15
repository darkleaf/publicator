(ns publicator.web.controllers.user.register
  (:require
   [publicator.use-cases.interactors.user.register :as interactor]))

(defn initial-params [_]
  [interactor/initial-params])

(defn process [{:keys [transit-params]}]
  [interactor/process transit-params])

(def routes
  #{[:get "/register" #'initial-params :user.register/initial-params]
    [:post "/register" #'process :user.register/process]})
