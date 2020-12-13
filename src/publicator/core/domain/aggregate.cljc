(ns publicator.core.domain.aggregate
  (:require
   [publicator.utils :as u]
   [medley.core :as m]
   [datascript.core :as d]
   [datascript.db :as d.db]))

(defonce schema (atom {:agg/id       {:agg/predicate int?}
                       :error/entity {:db/valueType :db.type/ref}
                       :error/attr   {:db/index true}}))

(defn- datascript-schema []
  (->> @schema
       (m/map-vals (fn [item] (m/filter-keys #(= "db" (namespace %)) item)))
       (m/filter-vals not-empty)))

(defn build [& tx-data]
  (-> (d/empty-db (datascript-schema))
      (d/db-with [[:db/add 1 :db/ident :root]])
      (d/db-with tx-data)))

(defn remove-errors [agg]
  (->> (d/datoms agg :aevt :error/entity)
       (map (fn [{:keys [e]}] [:db.fn/retractEntity e]))
       (d/db-with agg)))

(defn filter-datoms [agg allowed-attr?]
  (let [pred   (comp (some-fn #(= "db" (namespace %))
                              allowed-attr?)
                     :a)
        datoms (->> (d/datoms agg :eavt)
                    (filter pred))
        schema (:schema agg)]
    (d/init-db datoms schema)))

(defn include?
  ([agg e a]
   (-> agg (d/datoms :eavt e a  ) (seq) (boolean)))
  ([agg e a v]
   (-> agg (d/datoms :eavt e a v) (seq) (boolean))))

(defn root [agg]
  (d/entity agg :root))

(defn id [agg]
  (d/q '[:find ?id .
         :where
         [:root :agg/id ?id]]
       agg))

(defn- apply-predicate [p x]
  (cond
    (nil? p)          true
    (ifn? p)          (p x)
    (and (u/regexp? p)
         (string? x)) (re-matches p x)))

(defn predicate-validator [agg]
  (let [tx-data (for [[e a v] (d/datoms agg :aevt)
                      :let    [pred (get-in @schema [a :agg/predicate])]
                      :when   (not (apply-predicate pred v))]
                  {:error/type   :predicate
                   :error/entity e
                   :error/attr   a
                   :error/value  v})]
    (d/db-with agg tx-data)))

(defn validate [agg]
  (-> agg
      (predicate-validator)))

(defn has-errors? [agg]
  (boolean (seq (d/datoms agg :aevt :error/entity))))

(defn has-no-errors? [agg]
  (not (has-errors? agg)))

(defn check-errors [agg]
  (if (has-errors? agg)
    (throw (ex-info "Invalid aggregate"
                    {:agg agg}))
    agg))

(defn- entities-by-tag [agg tag]
  "the tag is an ident, ref, reveresed ref or attr-value pair"
  (cond
    (vector? tag)           (->> (apply d/datoms agg :avet tag)
                                 (map :e))
    (not (keyword? tag))    nil
    (d.db/reverse-ref? tag) (->> (d/datoms agg :avet (d.db/reverse-ref tag))
                                 (map :e))
    (d.db/ref? agg tag)     (->> (d/datoms agg :avet tag)
                                 (map :v))
    :ident                  (->> (d/datoms agg :avet :db/ident tag)
                                 (map :e))))

(defn required-attrs-validator [agg desc]
  (let [tx-data (for [[tag attrs] desc
                      e           (entities-by-tag agg tag)
                      a           attrs
                      :when       (empty? (d/datoms agg :eavt e a))]
                  {:error/type   :required
                   :error/entity e
                   :error/attr   a})]
    (d/db-with agg tx-data)))

(defn permitted-attrs-validator [agg permitted-attr?]
  (let [pred   (comp (some-fn #(#{"db" "error"} (namespace %))
                              permitted-attr?)
                     :a)
        errors (->> (d/datoms agg :eavt)
                    (remove pred)
                    (map (fn [[e a v]]
                           {:error/type   :rejected
                            :error/entity e
                            :error/attr   a
                            :error/value  v})))]
    (d/db-with agg errors)))
