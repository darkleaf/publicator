(ns publicator.components.pedestal
  (:require
   [publicator.controllers.registration :as registration]
   [com.stuartsierra.component :as component]
   [io.pedestal.http :as http]
   [io.pedestal.http.route :as route]))

(defn routes []
  (route/expand-routes
   (-> #{}
       (into (registration/routes)))))

(defn interceptor []
  {:name ::tmp-interceptor
   :enter   (fn [context]
              (prn context)
              context)})

(defrecord Pedestal [server]
  component/Lifecycle
  (start [this]
    (http/start server)
    this)
  (stop [this]
    (http/stop server)
    this))

(defn build []
  (let [server
        (-> {::http/routes (routes)
             ::http/join?  false
             ::http/type   :jetty
             ::http/port   4101

             ::http/secure-headers {:content-security-policy-settings
                                    {:object-src "none"}}}
            (http/default-interceptors)
            (update ::http/interceptors conj (interceptor))
            (http/create-server))]
    (->Pedestal server)))
