(ns publicator.web.presenters.layout
  (:require
   [publicator.use-cases.services.user-session :as user-session]
   [publicator.web.routing :as routing]
   [ring.middleware.anti-forgery :as anti-forgery]))

(defn present [req]
  (cond-> {:csrf anti-forgery/*anti-forgery-token*}
    (user-session/logged-in?)
    (assoc :log-out {:text   "Log out"
                     :url    (routing/path-for :user.log-out/process)})

    (user-session/logged-out?)
    (assoc :register {:text "Register"
                      :url  (routing/path-for :user.register/initial-params)})

    (user-session/logged-out?)
    (assoc :log-in {:text "Log in"
                    :url  (routing/path-for :user.log-in/initial-params)})))
