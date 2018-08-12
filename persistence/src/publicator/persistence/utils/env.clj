(ns publicator.persistence.utils.env)

(defn data-source-opts [env-name]
  (let [database-url                   (System/getenv env-name)
        pattern                        #"postgres://(\S+):(\S+)@(\S+):(\S+)/(\S+)"
        [_ user password host port path] (re-matches pattern  database-url)]
    {:jdbc-url (str "jdbc:postgresql://" host ":" port "/" path)
     :user     user
     :password password}))
