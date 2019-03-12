(ns publicator.domain.validators.uniqueness
  (:require
   [publicator.domain.abstractions.occupation :as occupation]
   [publicator.utils.datascript.validation :as validation]
   [datascript.core :as d]))

(defn validator
  ([attrs] (validator validation/all-q attrs))
  ([entities-q attrs]
   (fn [report]
     (let [db-before (:db-before report)
           db-after  (:db-after report)
           ids       (d/q entities-q db-after)
           changed   (d/q '{:find  [[?e ...]]
                            :in    [$before $after [?e ...] [?a ...]]
                            :where [[$after ?e ?a ?v]
                                    (not [$before ?e ?a ?v])]}
                          db-before db-after ids attrs)]
       (for [id    changed
             :let  [values    (d/pull db-after (vec attrs) id)
                    occupied? (occupation/*occupied* attrs values)]
             :when occupied?]
         {:type       ::not-unique
          :entity     id
          :attributes attrs
          :values     values})))))
