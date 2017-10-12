(ns publicator.interactors.abstractions.session)

(defprotocol Session
  (-get [this k])
  (-set! [this k v]))

(defn log-in! [session user-id]
  (-set! session ::user-id user-id))

(defn user-id [session]
  (-get session ::user-id))

(defn logged-in? [session]
  (boolean (user-id session)))

(defn logged-out? [session]
  (not (user-id session)))

(defn log-out! [session]
  (-set! session ::user-id nil))
