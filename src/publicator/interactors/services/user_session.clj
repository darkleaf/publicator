(ns publicator.interactors.services.user-session
  (:require
   [publicator.interactors.abstractions.session :as session]
   [publicator.interactors.abstractions.storage :as storage]))

(defn user []
  (storage/tx-get-one (session/get ::id)))

(defn logged-in? []
  (boolean (session/get ::id)))

(defn logged-out? []
  (not (logged-in?)))

(defn log-in! [user]
  (session/set! ::id (:id user)))

(defn log-out! []
  (session/set! ::id nil))
