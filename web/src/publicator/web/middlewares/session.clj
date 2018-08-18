(ns publicator.web.middlewares.session
  (:require
   [publicator.use-cases.abstractions.session :as session]))

(deftype Session [storage]
  session/Session
  (-get [_ k] (get @storage k))
  (-set! [_ k v] (swap! storage assoc k v)))

(defn wrap-session [handler]
  (fn [req]
    (let [storage (atom (get-in req [:session ::storage]))
          resp    (binding [session/*session* (Session. storage)]
                    (handler req))]
      (-> resp
          (assoc :session/key (:session/key req))
          (assoc :session (:session req))
          (assoc-in [:session ::storage] @storage)))))
