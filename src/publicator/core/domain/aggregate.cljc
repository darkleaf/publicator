(ns publicator.core.domain.aggregate
  (:require
   [publicator.utils :as u]
   [datascript.core :as d]
   [datascript.db :as d.db]))

(defonce schema (atom {:agg/id       {:agg/predicate pos-int?}
                       :error/entity {:db/valueType :db.type/ref}
                       :error/attr   {:db/index true}}))

(defn allocate [& tx-data]
  (-> (d/empty-db @schema)
      (d/db-with [[:db/add 1 :db/ident :root]])
      (d/db-with tx-data)))

(defn remove-errors [agg]
  (->> (d/datoms agg :aevt :error/entity)
       (map (fn [{:keys [e]}] [:db.fn/retractEntity e]))
       (d/db-with agg)))

(defn- apply-predicate [p x]
  (cond
    (nil? p)          true
    (ifn? p)          (p x)
    (and (u/regexp? p)
         (string? x)) (re-matches p x)))

(defn predicate-validator [agg]
  (let [tx-data (for [[e a v] (d/datoms agg :aevt)
                      :let    [pred (get-in agg [:schema a :agg/predicate])]
                      :when   (not (apply-predicate pred v))]
                  {:error/type   :predicate
                   :error/entity e
                   :error/attr   a
                   :error/value  v})]
    (d/db-with agg tx-data)))

(defn uniq-validator [agg]
  (let [errors-for-attr (fn [a]
                          (->> (d/datoms agg :aevt a)
                               (reduce (fn [{:keys [seen], :as acc} [e _ v]]
                                         (if (seen v)
                                           (update acc :errors conj {:error/type   :uniq
                                                                     :error/entity e
                                                                     :error/attr   a
                                                                     :error/value  v})
                                           (update acc :seen conj v)))
                                       {:seen #{}, :errors []})
                               :errors))
        uniq-attrs      (reduce-kv (fn [acc attr desc]
                                     (if (:agg/uniq desc)
                                       (conj acc attr)
                                       acc))
                                   #{} (:schema agg))
        tx-data         (mapcat errors-for-attr uniq-attrs)]
    (d/db-with agg tx-data)))

(defn validate [agg]
  (-> agg
      (remove-errors)
      (predicate-validator)
      (uniq-validator)))

(defn has-errors? [agg]
  (boolean (seq (d/datoms agg :aevt :error/entity))))

(defn has-no-errors? [agg]
  (not (has-errors? agg)))

(defn check-errors [agg]
  (if (has-errors? agg)
    (throw (ex-info "Invalid aggregate"
                    {:agg agg}))
    agg))

(defn check-report-tx-data! [report allowed-datom?]
  (if-some [extra-datoms (->> report
                              :tx-data
                              (remove allowed-datom?)
                              (seq))]
    (throw (ex-info "Extra datoms"
                    {:extra extra-datoms}))
    report))

(defn filter-datoms [agg allowed-datom?]
  (let [datoms (->> (d/datoms agg :eavt)
                    (filter allowed-datom?))
        schema (:schema agg)]
    (d/init-db datoms schema)))

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

(defn required-validator [agg desc]
  (let [tx-data (for [[tag attrs] desc
                      e           (entities-by-tag agg tag)
                      a           attrs
                      :when       (empty? (d/datoms agg :eavt e a))]
                  {:error/type   :required
                   :error/entity e
                   :error/attr   a})]
    (d/db-with agg tx-data)))

(defn count-validator [agg attr expected-count]
  (let [actual-count (->> (d/datoms agg :aevt attr)
                          (count))]
    (if (= expected-count actual-count)
      agg
      (d/db-with agg [{:error/type           :count
                       :error/entity         :root
                       :error/attr           attr
                       :error/actual-count   actual-count
                       :error/expected-count expected-count}]))))

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
