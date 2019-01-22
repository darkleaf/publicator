(ns publicator-ext.domain.aggregates.article
  (:require
   [publicator-ext.domain.abstractions.aggregate :as aggregate]
   [publicator-ext.domain.aggregates.publication :as publication]
   [publicator-ext.domain.errors :as errors]
   [clojure.spec.alpha :as s]
   [publicator-ext.domain.abstractions.id-generator :as id-generator]))

(s/def :article/image-url string?)
(s/def :entity.type/article
  (s/merge ::publication/publication
           (s/keys :opt [:article/image-url])))

(s/def :article.translation/content string?)
(s/def :entity.type/article.translation
  (s/merge ::publication/translation
           (s/keys :opt [:article.translation/content])))

(def ^:const +schema+ (merge publication/+schema+))

;; можно написать спеку на функцию `build`,
;; но лучше дождаться `s/schema` и `s/select`
(defn build [params]
  (let [params (merge publication/+initial-params+
                      params
                      {:aggregate/id (id-generator/generate :publication)
                       :entity/type  :entity.type/article})]
    (aggregate/build +schema+ params)))

(defn add-translation [article params]
  (let [params (merge publication/+translation-initial-params+
                      params
                      {:entity/type :entity.type/article.translation})]
    (aggregate/update article [params])))

(defmethod aggregate/errors :entity.type/article [article]
  (-> (errors/build article)
      (publication/append-errors)
      (errors/attributes '[[?e :db/ident :root]
                           [?e :publication/state :active]
                           [?translation :publication.translation/publication ?e]
                           [?translation :publication.translation/state :published]]
                         [[:article/image-url not-empty {:type :must-be-filled}]])
      (errors/attributes '[[?e :publication.translation/state :published]
                           [?e :publication.translation/publication ?root]
                           [?root :publication/state :active]]
                         [[:article.translation/content not-empty {:type :must-be-filled}]])
      (errors/extract)))
