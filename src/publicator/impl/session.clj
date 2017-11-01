(ns publicator.impl.session
  (:require
   [ring.middleware.session :as ring.session]
   [publicator.interactors.abstractions.session :as session]))

(deftype Session [storage]
  session/Session
  (-get [_ k] (get @storage k))
  (-set! [_ k v] (swap! storage assoc k v)))

(defn wrap-session [handler]
  (ring.session/wrap-session ;; todo: move from memory to cookie
   (fn [req]
     (let [storage (atom (:session req))
           resp    (binding [session/*session* (Session. storage)]
                     (handler req))
           resp    (assoc resp :session/key (:session/key req))
           resp    (assoc resp :session @storage)]
       resp))))
