(ns publicator.core
  (:require
   [publicator.init]
   [com.stuartsierra.component :as component]
   [publicator.components.data-source :as data-source]
   [publicator.components.jetty :as jetty]
   [publicator.db.migration :as migration]

   [publicator.impl.hasher :as hasher]
   [publicator.impl.id-generator :as id-generator]
   [publicator.impl.post-queries :as post-q]
   [publicator.impl.storage :as storage]
   [publicator.impl.storage.post-manager :as post-manager]
   [publicator.impl.storage.user-manager :as user-manager]
   [publicator.impl.user-queries :as user-q]))

(defrecord Impls [data-source binding-map]
  component/Lifecycle
  (start [this]
    (let [data-source (:data-source data-source)
          managers    (merge
                       (post-manager/manager)
                       (user-manager/manager))
          binding-map (merge
                       (storage/binding-map data-source managers)
                       (user-q/binding-map data-source)
                       (post-q/binding-map data-source)
                       (hasher/binding-map)
                       (id-generator/binding-map data-source))]
      (assoc this :binding-map binding-map)))
  (stop [this]
    (assoc this :binding-map nil)))

(defn build-impls []
  (Impls. nil nil))

(defn data-source-opts []
  (let [database-url                   (System/getenv "DATABASE_URL")
        pattern                        #"postgres://(\S+):(\S+)@(\S+):(\S+)/(\S+)"
        [_ user password host port db] (re-matches pattern  database-url)]
    {:jdbc-url (str "jdbc:postgresql://" host ":" port "/" db "?sslmode=require")
     :user     user
     :password password}))

(defrecord Migration [data-source]
  component/Lifecycle
  (start [this]
    (migration/migrate (:data-source data-source))
    this)
  (stop [this]
    this))

(defn build []
  (component/system-map
   :data-source (data-source/build (data-source-opts))
   :implementations (component/using
                     (build-impls)
                     [:data-source])
   :jetty (component/using
           (jetty/build {:host "0.0.0.0"
                         :port (-> "PORT" System/getenv bigint)})
           [:implementations])
   :migration (component/using
               (->Migration nil)
               [:data-source])))

(defn -main [& _]
  ;; todo: stop
  (let [system (build)]
    (component/start system)))
