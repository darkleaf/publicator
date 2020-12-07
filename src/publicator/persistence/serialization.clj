(ns publicator.persistence.serialization
  (:require
   [datascript.core :as d]
   [medley.core :as m]
   [publicator.core.domain.aggregate :as agg]
   [publicator.utils :refer [<<-]]))

(defn- parse-field [field]
  (<<-
   (if-some [[_ lang attr] (re-matches #"\A(\w\w)\$(.+)" field)]
     {:kind :translation
      :lang (keyword lang)
      :attr (keyword attr)})
   (if-some [[_ tag attr] (re-matches #"\A(es|vs)#(.+)" field)]
     {:kind :nested
      :tag  (keyword tag)
      :attr (keyword attr)})
   (if (re-matches #"[^\$#]+" field)
     {:kind :root
      :attr (keyword field)})
   (throw (ex-info "Unexpected field format" {:field field}))))

(defn- root-attr->field [attr]
  (str (symbol attr)))

(defn- translation-attr->field [lang attr]
  (str (symbol lang) "$" (symbol attr)))

(defn- nested-attr->field [tag attr]
  (str (symbol tag) "#" (symbol attr)))

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
                                    (assoc acc
                                           (nested-attr->field :es a) es
                                           (nested-attr->field :vs a) vs))
                                  {}))]
    (merge root translations nested)))

(defn row->agg [row]
  (let [items            (for [[field data] row]
                           (assoc (parse-field field) :data data))
        {:keys [root
                translation
                nested]} (group-by :kind items)
        root-tx          (for [{:keys [attr data]} root]
                           {:db/ident :root attr data})
        translation-tx   (for [[lang items] (group-by :lang translation)]
                           (reduce (fn [acc {:keys [attr data]}]
                                     (assoc acc attr data))
                                   {:translation/lang lang
                                    :translation/root :root}
                                   items))
        nested-tx        (for [[attr items] (group-by :attr nested)
                               :let         [{:keys [es vs]} (->> items
                                                                  (m/index-by :tag)
                                                                  (m/map-vals :data))]
                               [e v]        (map vector es vs)]
                           [:db/add e attr v])
        tx-data          (concat root-tx translation-tx nested-tx)
        agg              (agg/build)]
    (d/db-with agg tx-data)))
