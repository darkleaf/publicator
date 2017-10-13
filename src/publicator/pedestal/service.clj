(ns publicator.pedestal.service
  (:require
   [io.pedestal.http :as http]
   [io.pedestal.http.ring-middlewares :as middlewares]
   [io.pedestal.http.body-params :as body-params]
   [publicator.pedestal.routes :as routes]
   [publicator.pedestal.interactor-ctx :as interactor-ctx]
   [publicator.pedestal.session :as session]))

(defn build [interactor-ctx]
  (-> {::http/routes (routes/build)
       ::http/join?  false
       ::http/type   :jetty
       ::http/port   4101
       ::http/secure-headers {:content-security-policy-settings
                              {:object-src "none"}}}
      (http/default-interceptors)
      (update ::http/interceptors into
              [(middlewares/session) ;;in memory sessions
               (session/build)
               (interactor-ctx/build interactor-ctx)
               (body-params/body-params)])))
