(ns publicator.interactors.helpers.user-session
  (:require
   [publicator.interactors.abstractions.session :as session]))

(defn log-in [session user-id]
  (session/write! session ::user-id user-id))

(defn user-id [session]
  (session/read session ::user-id))

(defn logged-in? [session]
  (boolean (user-id session)))

(defn logged-out? [session]
  (not (user-id session)))

(defn log-out [session]
  (session/write! session ::user-id nil))
