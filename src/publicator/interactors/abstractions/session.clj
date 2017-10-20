(ns publicator.interactors.abstractions.session)

(defprotocol Session
  (-get [this k])
  (-set! [this k v]))

(declare ^:dynamic *session*)

(defn log-in! [user-id]
  (-set! *session* ::user-id user-id))

(defn user-id []
  (-get *session* ::user-id))

(defn logged-in? []
  (boolean (user-id)))

(defn logged-out? []
  (not (user-id)))

(defn log-out! []
  (-set! *session* ::user-id nil))
