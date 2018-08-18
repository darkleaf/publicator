(ns publicator.main.core
  (:require
   [com.stuartsierra.component :as component]
   [signal.handler :as signal]
   [publicator.web.components.jetty :as jetty]
   [publicator.web.components.handler :as handler]
   [publicator.persistence.components.data-source :as data-source]
   [publicator.persistence.components.migration :as migration]
   [publicator.persistence.utils.env :as env]
   [publicator.main.binding-map :as binding-map]))

(defn http-opts []
  {:host "0.0.0.0"
   :port (bigint (System/getenv "PORT"))})

(defn -main [& _]
  (let [system (component/system-map
                :data-source (data-source/build (env/data-source-opts "DATABASE_URL"))
                :migration (component/using (migration/build) [:data-source])
                :binding-map (component/using (binding-map/build) [:data-source])
                :handler (component/using (handler/build) [:binding-map])
                :jetty (component/using (jetty/build (http-opts)) [:binding-map :handler]))
        system (component/start system)]
    (signal/with-handler :term
      (prn "caught SIGTERM, quitting.")
      (component/stop system)
      (System/exit 0))))
