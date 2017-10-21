(ns publicator.interactors.user.log-out
  (:require
   [publicator.interactors.abstractions.user-queries :as user-q]
   [publicator.interactors.abstractions.session :as session]
   [publicator.domain.user :as user]
   [better-cond.core :as b]
   [clojure.spec.alpha :as s]))

(defn- check-logged-in []
  (when (session/logged-out?)
    {:type ::already-logged-out}))

(defn- log-out! []
  (session/log-out!))

(b/defnc process []
  :let [err (check-logged-in)]
  (some? err) err
  :do (log-out!)
  {:type ::processed})
