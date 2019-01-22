(ns publicator-ext.domain.aggregates.gallery
  (:require
   [publicator-ext.domain.abstractions.aggregate :as aggregate]
   [publicator-ext.domain.abstractions.id-generator :as id-generator]
   [publicator-ext.domain.aggregates.publication :as publication]
   [publicator-ext.domain.errors :as errors]
   [clojure.spec.alpha :as s]))

(s/def :gallery/image-urls (s/coll-of string?))
(s/def :entity.type/gallery
  (s/merge ::publication/publication
           (s/keys :opt [:article/image-urls])))

(s/def :entity.type/gallery.translation
  (s/merge ::publication/translation))

(def ^:const +schema+ (merge publication/+schema+
                             {:gallery/image-urls {:db/cardinality :db.cardinality/many}}))

(defn build [params]
  (let [params (merge publication/+initial-params+
                      params
                      {:aggregate/id (id-generator/generate :publication)
                       :entity/type  :entity.type/gallery})]
    (aggregate/build +schema+ params)))

(defn add-translation [gallery params]
  (let [params (merge publication/+translation-initial-params+
                      params
                      {:entity/type :entity.type/gallery.translation})]
    (aggregate/update gallery [params])))

(defmethod aggregate/errors :entity.type/gallery [article]
  (-> (errors/build article)
      (publication/append-errors)
      (errors/attributes '[[?e :db/ident :root]
                           [?e :publication/state :active]
                           [?translation :publication.translation/publication ?e]
                           [?translation :publication.translation/state :published]]
                         [[:gallery/image-urls not-empty {:type :must-be-filled}]])
      (errors/extract)))
