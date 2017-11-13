(ns publicator.interactors.services.user-session
  (:require
   [publicator.interactors.abstractions.session :as session]
   [publicator.interactors.abstractions.storage :as storage]))

(defn user-id []
  (session/get ::id))

(defn user []
  (when-let [id (user-id)]
    (storage/tx-get-one id)))

(defn logged-in? []
  (boolean (user-id)))

(defn logged-out? []
  (not (logged-in?)))

(defn log-in! [user]
  (session/set! ::id (:id user)))

(defn log-out! []
  (session/set! ::id nil))
