(ns publicator.components.pedestal
  (:require
   [publicator.controllers.registration :as registration]
   [publicator.interactors.abstractions.session :as abstractions.session]
   [com.stuartsierra.component :as component]
   [io.pedestal.http :as http]
   [io.pedestal.http.route :as route]
   [io.pedestal.http.ring-middlewares :as middlewares]
   [io.pedestal.http.body-params :as body-params]))

(defn routes []
  (route/expand-routes
   (-> #{}
       (into (registration/routes)))))

(defn service-map []
  (-> {::http/routes (routes)
       ::http/join?  false
       ::http/type   :jetty
       ::http/port   4101

       ::http/secure-headers {:content-security-policy-settings
                              {:object-src "none"}}}
      (http/default-interceptors)))

(deftype Session [tmp]
  abstractions.session/Session
  (-get [_ k] (get @tmp k))
  (-set! [_ k v] (swap! tmp assoc k v)))

(defn session-impl-interceptor []
  {:name  ::session-impl-interceptor
   :enter (fn [context]
            (let [tmp-session-storage (atom (get-in context [:request :session]))
                  session             (Session. tmp-session-storage)]
              (-> context
                  (assoc ::tmp-session-storage tmp-session-storage)
                  (assoc-in [:request :impl ::abstractions.session/session] session))))
   :leave (fn [context]
            (let [tmp-session-storage (::tmp-session-storage context)]
              (-> context
                  (assoc-in [:response :session/key]
                            (get-in context [:request :session/key]))
                  (assoc-in [:response :session] @tmp-session-storage))))})

(defn impl-interceptor [impl]
  {:name  ::add-impl-interceptor
   :enter (fn [context]
            (update-in context [:request :impl]
                       merge impl))})

(defrecord Pedestal [impl server]
  component/Lifecycle
  (start [this]
    (if server
      this
      (assoc this :server
             (-> (service-map)
                 (update ::http/interceptors into
                         [(impl-interceptor (:impl impl))
                          (middlewares/session) ;;in memory sessions
                          (session-impl-interceptor)
                          (body-params/body-params)])
                 (http/create-server)
                 (http/start)))))
  (stop [this]
    (if server
      (do
        (http/stop server)
        (assoc this :server nil))
      this)))

(defn build []
  (Pedestal. nil nil))
