(ns publicator.persistence.serialization
  (:require
   [clojure.string :as str]
   [datascript.core :as d]
   [medley.core :as m]
   [publicator.core.domain.aggregate :as agg]))

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
    (-> (agg/build)
        (d/db-with (cons root nested)))))
