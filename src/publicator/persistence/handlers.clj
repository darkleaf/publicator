(ns publicator.persistence.handlers
  (:require
   [datascript.core :as d]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :as jdbc.sql]
   [next.jdbc.quoted :as jdbc.quoted]
   [next.jdbc.result-set :as jdbc.rs]
   [publicator.core.domain.aggregate :as agg])
  (:import
   [java.sql Array]))

(extend-protocol jdbc.rs/ReadableColumn
  Array
  (read-column-by-label [^Array v _]    (vec (.getArray v)))
  (read-column-by-index [^Array v _ _]  (vec (.getArray v))))

(def opts {:table-fn   jdbc.quoted/postgres
           :column-fn  jdbc.quoted/postgres
           :builder-fn jdbc.rs/as-unqualified-maps})

(defn wrap-tx [perform transactable]
  (fn [handlers continuation [ctx args]]
    (jdbc/with-transaction [tx transactable]
      (let [ctx (assoc ctx ::tx tx)]
        (perform handlers continuation [ctx args])))))

(defn- wrap-context-reader [handler]
  (fn [context & args]
    [context (apply handler context args)]))

(defn- row->user [row]
  (let [user                (-> row
                                (select-keys [:agg/id :user/state :user/admin? :user/author?
                                              :user/login :user/password-digest])
                                (update :user/state keyword)
                                (merge {:db/ident :root}))
        author-translations (map-indexed (fn [idx id]
                                           {:db/id                     id
                                            :author.translation/author :root
                                            :author.translation/lang
                                            (-> row :author.translation/lang (get idx) keyword)
                                            :author.translation/first-name
                                            (-> row :author.translation/first-name (get idx))
                                            :author.translation/last-name
                                            (-> row :author.translation/last-name (get idx))})
                                         (:author.translation row))]
    (-> (agg/build)
        (d/db-with (cons user author-translations)))))


(defn- user-get-by-id [{::keys [tx]} id]
  (some-> (jdbc.sql/get-by-id tx "user" id "agg/id" opts)
          (row->user)))

(defn handlers []
  {:persistence.user/get-by-id (-> user-get-by-id (wrap-context-reader))})
