(ns publicator.domain.aggregate
  (:require
   [datascript.core :as d]))

(defn rules [agg]
  (-> agg meta ::rules))

;; (defn validate [agg]
;;   ((-> agg meta ::validate) agg))

(defn extend-schema [agg ext]
  (let [schema (merge ext (:schema agg))
        datoms (d/datoms agg :eavt)]
    (d/init-db datoms schema)))

(defn decorate [agg decorators-map]
  (let [merge-fn (fn [val decorator] (decorator val))]
    (vary-meta agg #(merge-with merge-fn % decorators-map))))

(def blank
  (-> (d/empty-db)
      (d/db-with [[:db/add 1 :db/ident :root]])
      (with-meta
        {::validate identity
         ::rules    '[[(root ?e)
                       [?e :db/ident :root]]]})))

(defmulti reducer (fn [agg [kind & rest]] kind))

(defn root [agg]
  (d/entity agg :root))

(def ^{:arglists '([agg tx-data])} agg-with d/db-with)

(defn q
  ([agg query] (q agg query []))
  ([agg query inputs]
   (let [query  (update query :in (fn [in] (concat '[$ %] in)))
         inputs (concat [agg (rules agg)] inputs)]
     (apply d/q query inputs))))

(defmethod reducer :agg/add-attribute [agg [attr {:keys [entity value]}]]
  (agg-with agg [[:db/add entity attr value]]))
