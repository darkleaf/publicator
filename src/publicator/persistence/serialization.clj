(ns publicator.persistence.serialization
  (:require
   [datascript.core :as d]
   [datascript.db :as d.db]
   [medley.core :as m]
   [publicator.core.domain.aggregate :as agg]
   [publicator.utils :refer [<<-]]))

(defn- parse-field [field]
  (<<-
   (if-some [[_ lang attr] (re-matches #"\A(.+?)\$(.+)" field)]
     {:kind :translation
      :lang (keyword lang)
      :attr (keyword attr)})
   (if-some [[_ attr] (re-matches #"\A#(.+)" field)]
     {:kind :nested
      :attr (keyword attr)})
   {:kind :root
    :attr (keyword field)}))

(defn- root-attr->field [attr]
  (str (symbol attr)))

(defn- translation-attr->field [lang attr]
  (str (symbol lang) "$" (symbol attr)))

(defn- nested-attr->field [attr]
  (str "#" (symbol attr)))

(defn agg->row [agg]
  (let [root         (->> (d/pull agg '[*] :root)
                          (m/remove-keys #(= "db" (namespace %)))
                          (m/map-keys root-attr->field))
        translations (->> agg
                          (d/q '[:find ?lang (pull ?t [*])
                                 :where
                                 [?t :translation/root :root]
                                 [?t :translation/lang ?lang]])
                          (map (fn [[lang attrs]]
                                 (->> attrs
                                      (m/remove-keys #(= "translation" (namespace %)))
                                      (m/map-keys (partial translation-attr->field lang)))))
                          (reduce merge))
        nested       (->> agg
                          (d/q '[:find ?a (aggregate ?vec ?e) (aggregate ?vec ?v)
                                 :in ?vec $
                                 :where
                                 [?e ?a ?v]
                                 (not [?e :db/ident :root])
                                 (not [?e :translation/root :root])]
                               vec)
                          (reduce (fn [acc [a es vs]]
                                    (assoc acc (nested-attr->field a) [es vs]))
                                  {}))]
    (merge root translations nested)))

(defn row->agg [row]
  (let [agg      (agg/build)
        pair->tx (fn [[field data]]
                   (let [{:keys [kind attr lang]} (parse-field field)]
                     (case kind
                       :root        [[:db/add :root attr data]]
                       :translation (<<-
                                     (let [id-field (translation-attr->field lang :db/id)
                                           id       (get row id-field)])
                                     (if (= :db/id attr)
                                       [[:db/add id :translation/lang lang]
                                        [:db/add id :translation/root :root]])
                                     (if (d.db/multival? agg attr)
                                       (for [v data]
                                         [:db/add id attr v]))
                                     [[:db/add id attr data]])
                       :nested      (map (fn [e v]
                                           [:db/add e attr v])
                                         (first data) (second data)))))
        tx       (mapcat pair->tx row)]
    (d/db-with agg tx)))
