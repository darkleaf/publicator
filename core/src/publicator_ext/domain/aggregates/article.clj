(ns publicator-ext.domain.aggregates.article
  (:require
   [publicator-ext.domain.abstractions.aggregate :as aggregate]
   [publicator-ext.domain.aggregates.publication :as publication]
   [publicator-ext.domain.errors :as errors]
   [clojure.spec.alpha :as s]
   [publicator-ext.domain.abstractions.id-generator :as id-generator]))

(s/def :article/image-url string?)
(s/def :entity.type/article
  (s/merge :entity.type/publication
           (s/keys :opt [:article/image-url])))

(s/def :article.translation/content string?)
(s/def :entity.type/article.translation
  (s/merge :entity.type/publication.translation
           (s/keys :opt [:article.translation/content])))

(def ^:const +schema+ (merge publication/+schema+))

(defn build [params]
  (let [params (-> params
                   publication/prepare-initial-params
                   (assoc :aggregate/id (id-generator/generate :publication)
                          :entity/type  :entity.type/article))]
    (aggregate/build +schema+ params)))

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
