(ns publicator.domain.aggregates.author
  (:require
   [publicator.domain.aggregate :as agg]
   [publicator.domain.languages :as langs]
   [publicator.util :as u]
   [darkleaf.multidecorators :as md]))

(def states #{:active :archived})
(def stream-participation-roles #{:regular :admin})

(defn rules-decorator [super agg]
  (conj (super agg)
        '[(translation ?e)
          [?e :author.translation/author :root]]
        '[(stream-participation ?e)
          [?e :author.stream-participation/author :root]]))
(md/decorate agg/rules :agg/author #'rules-decorator)

(defn validate-decorator [super agg]
  (-> (super agg)
      (agg/predicate-validator 'root
                               {:author/state states})
      (agg/required-validator  'root
                               #{:author/state})
      (agg/query-validator     'root
                               '[:find [?lang ...]
                                 :with ?trans
                                 :where
                                 [?trans :author.translation/author ?e]
                                 [?trans :author.translation/lang ?lang]]
                               #'langs/all-languages?)
      (agg/query-validator     'root
                               '[:find [?stream-id ...]
                                 :with ?part
                                 :where
                                 [?part :author.stream-participation/author ?e]
                                 [?part :author.stream-participation/stream-id ?stream-id]]
                               u/distinct-coll?)

      (agg/predicate-validator 'translation
                               {:author.translation/lang       langs/languages
                                :author.translation/first-name #".{1,255}"
                                :author.translation/last-name  #".{1,255}"})
      (agg/required-validator  'translation
                               #{:author.translation/lang
                                 :author.translation/first-name
                                 :author.translation/last-name})

      (agg/predicate-validator 'stream-participation
                               {:author.stream-participation/role      stream-participation-roles
                                :author.stream-participation/stream-id #'pos-int?})
      (agg/required-validator  'stream-participation
                               #{:author.stream-participation/role
                                 :author.stream-participation/stream-id})))
(md/decorate agg/validate :agg/author #'validate-decorator)

(def blank
  (-> agg/blank
      (vary-meta assoc :type :agg/author)
      (agg/extend-schema {:author.translation/author          {:db/valueType :db.type/ref}
                          :author.stream-participation/author {:db/valueType :db.type/ref}})))
