(ns publicator.persistence.handlers
  (:require
   [datascript.core :as d]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :as jdbc.sql]
   [next.jdbc.quoted :as jdbc.quoted]
   [next.jdbc.result-set :as jdbc.rs]
   [publicator.core.domain.aggregate :as agg]
   [publicator.utils :as u])
  (:import
   [java.sql Array]))

(extend-protocol jdbc.rs/ReadableColumn
  Array
  (read-column-by-label [^Array v _]    (.getArray v))
  (read-column-by-index [^Array v _ _]  (.getArray v)))

(defn extract-nested [entities-key keys row]
  (let [entities (get row entities-key)
        f        (fn [idx id]
                   (reduce (fn [acc key]
                             (assoc acc key (get-in row [key idx])))
                           {:db/id id}
                           keys))]
    (map-indexed f entities)))

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
        author-translations (->> row
                                 (extract-nested :author.translation
                                                 #{:author.translation/lang
                                                   :author.translation/first-name
                                                   :author.translation/last-name})
                                 (map (u/fn-> (update :author.translation/lang keyword)
                                              (assoc :author.translation/author :root))))]
    (-> (agg/build)
        (d/db-with (cons user author-translations)))))


(defn- user-get-by-id [{::keys [tx]} id]
  (some-> (jdbc.sql/get-by-id tx "user" id "agg/id" opts)
          (row->user)))

(defn handlers []
  {:persistence.user/get-by-id (-> user-get-by-id (wrap-context-reader))})
