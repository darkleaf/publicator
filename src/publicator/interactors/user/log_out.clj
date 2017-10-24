(ns publicator.interactors.user.log-out
  (:require
   [publicator.interactors.abstractions.user-queries :as user-q]
   [publicator.interactors.services.user-session :as user-session]
   [publicator.domain.user :as user]
   [better-cond.core :as b]
   [clojure.spec.alpha :as s]))

(defn- check-logged-in []
  (when (user-session/logged-out?)
    {:type ::already-logged-out}))

(b/defnc process []
  :let [err (check-logged-in)]
  (some? err) err
  :do (user-session/log-out!)
  {:type ::processed})
