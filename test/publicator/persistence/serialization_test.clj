(ns publicator.persistence.serialization-test
  (:require
   [publicator.persistence.serialization :as sut]
   [clojure.test :as t]
   [datascript.core :as d]
   [medley.core :as m]
   [clojure.string :as str]))

(defn agg->row [agg]
  (let [root   (->> (d/pull agg '[*] 1)
                    (m/remove-keys #(= "db" (namespace %)))
                    (m/map-keys #(str "r" %)))
        conjv  (fnil conj [])
        nested (reduce (fn [acc [e a v]]
                         (-> acc
                             (update (str "e" a) conjv e)
                             (update (str "v" a) conjv v)))
                       {}
                       (d/seek-datoms agg :eavt 2))]
    (merge root nested)))

(defn row->agg [row]
  (let [root-tag?   #(str/starts-with? % "r")
        tag->attr    #(-> % (subs 2) (keyword))
        root         (->> row
                          (m/filter-keys root-tag?)
                          (m/map-keys tag->attr)
                          (merge {:db/id 1 :db/ident :root}))
        nested-attrs (->> row
                          keys
                          (remove root-tag?)
                          (map tag->attr)
                          (set))
        nested       (mapcat (fn [attr]
                               (let [es (get row (str "e" attr))
                                     vs (get row (str "v" attr))]
                                 (map d/datom es (repeat attr) vs)))
                             nested-attrs)]
    ;; todo: agg
    (-> (d/empty-db {:nested/ref  {:db/valueType :db.type/ref}
                     :common/tags {:db/cardinality :db.cardinality/many}})
        (d/db-with (cons root nested)))))



(t/deftest ok
  (let [agg (-> (d/empty-db {:nested/ref  {:db/valueType :db.type/ref}
                             :common/tags {:db/cardinality :db.cardinality/many}})
                (d/db-with [{:db/ident    :root
                             :root/a      "x"
                             :common/tags [:a :b]}
                            {:nested/a    "x"
                             :nested/ref  :root
                             :common/tags [:a :c]}
                            {:nested/a    "y"
                             :nested/b    "y"
                             :nested/ref  :root
                             :common/tags [:c :d]}]))
        row {"r:common/tags" [:a :b]
             "r:root/a"      "x"
             "e:nested/a"    [2 3]
             "v:nested/a"    ["x" "y"]
             "e:nested/b"    [3]
             "v:nested/b"    ["y"]
             "e:nested/ref"  [2 3]
             "v:nested/ref"  [1 1]
             "e:common/tags" [2 2 3 3]
             "v:common/tags" [:a :c :c :d]}]
    (t/is (= row (agg->row agg)))
    (t/is (= agg (row->agg row)))))
