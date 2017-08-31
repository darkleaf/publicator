(ns publicator.interactors.abstractions.session
  (:require
   [clojure.spec.alpha :as s]))

(defprotocol Session
  (-get [this k])
  (-set! [this k v]))

(s/def ::session #(satisfies? Session %))

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
