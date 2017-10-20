(ns publicator.pedestal.service
  (:require
   [io.pedestal.http :as http]
   [io.pedestal.http
    [ring-middlewares :as ring-middlewares]
    [body-params :as body-params]]
   [publicator.pedestal
    [routes :as routes]
    [session :as session]]
   [publicator.web.layout.interceptor :as layout]))

(defn- binding-interceptor [binding-map]
  {:name  ::binding
   :enter #(update % :bindings merge binding-map)})

(defn build [binding-map]
  (-> {::http/routes (routes/build)
       ::http/join?  false
       ::http/type   :jetty
       ::http/port   4101
       ::http/secure-headers {:content-security-policy-settings
                              {:object-src "none"}}}
      (http/default-interceptors)
      (update ::http/interceptors into
              [(ring-middlewares/session) ;;in memory sessions
               (session/build)
               (binding-interceptor binding-map)
               (body-params/body-params)
               layout/layout])))
