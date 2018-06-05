(ns publicator.persistence.storage
  (:require
   [jdbc.core :as jdbc]
   [publicator.use-cases.abstractions.storage :as storage]
   [publicator.domain.abstractions.aggregate :as aggregate]
   [publicator.domain.abstractions.id-generator :as id-generator]
   [publicator.domain.identity :as identity]
   [publicator.utils.ext :as ext]
   [clojure.spec.alpha :as s])
  (:import
   [java.util.concurrent TimeoutException]
   [java.time Instant]))

(s/def ::version some?)
(s/def ::versioned-id (s/keys :req-un [::id-generator/id ::version]))
(s/def ::versioned-aggregate (s/keys :req-un [::aggregate/aggregate ::version]))

;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

(defprotocol Mapper
  (-lock   [this conn ids])
  (-select [this conn ids])
  (-insert [this conn aggregates])
  (-delete [this conn ids]))

(s/def ::mapper #(satisfies? Mapper %))

(s/fdef lock
  :args (s/cat :this ::mapper, :conn any?, :ids (s/coll-of ::id-generator/id))
  :ret (s/coll-of ::versioned-id))

(s/fdef select
  :args (s/cat :this ::mapper, :conn any?, :ids (s/coll-of ::id-generator/id))
  :ret (s/coll-of ::versioned-aggregate))

(s/fdef insert
  :args (s/cat :this ::mapper, :conn any?, :aggregates (s/coll-of ::aggregate/aggregate))
  :ret any?)

(s/fdef delete
  :args (s/cat :this ::mapper, :conn any?, :ids (s/coll-of ::id-generator/id))
  :ret any?)

(defn- default-for-empty [f default]
  (fn [this conn coll]
    (if (empty? coll)
      default
      (f this conn coll))))

(def lock   (default-for-empty -lock   []))
(def select (default-for-empty -select []))
(def insert (default-for-empty -insert nil))
(def delete (default-for-empty -delete nil))

;; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

(defrecord Transaction [data-source mappers identity-map]
  storage/Transaction
  (get-many [this ids]
    (with-open [conn (jdbc/connection data-source)]
      (let [ids-for-select (remove #(contains? @identity-map %) ids)
            selected       (->> mappers
                                (vals)
                                (mapcat #(select % conn ids-for-select))
                                (map (fn [{:keys [aggregate version]}]
                                       (let [iaggregate (identity/build aggregate)]
                                         (alter-meta! iaggregate assoc
                                                      ::version version
                                                      ::initial aggregate)
                                         iaggregate)))
                                (group-by #(-> % deref aggregate/id))
                                (ext/map-vals first))]
        ;; Здесь принципиально использование reverse-merge,
        ;; т.к. другой поток может успеть извлечь данные из базы,
        ;; создать объект-идентичность, записать его в identity map
        ;; и сделать в нем изменения.
        ;; Если использовать merge, то этот поток затрет идентчиность
        ;; другим объектом-идентичностью с начальным состоянием.
        ;; Фактически это нарушает саму идею identity-map -
        ;; сопоставление ссылки на объект с его идентификатором
        (-> identity-map
            (swap! ext/reverse-merge selected)
            (select-keys ids)))))

  (create [this state]
    (let [id     (aggregate/id state)
          istate (identity/build state)]
      (swap! identity-map assoc id istate)
      istate)))

(defn- build-tx [data-source mappers]
  (Transaction. data-source mappers (atom {})))

(defn- need-insert? [identity]
  (not= @identity
        (-> identity meta ::initial)))

(defn- need-delete? [identity]
  (let [initial (-> identity meta ::initial)]
    (and (some? initial)
         (not= @identity initial))))

(defn- lock-all [conn mappers identities]
  (let [ids      (->> identities
                      (vals)
                      (filter need-delete?)
                      (map deref)
                      (map aggregate/id))
        versions (->> mappers
                      (vals)
                      (mapcat #(lock % conn ids))
                      (group-by :id)
                      (ext/map-vals #(-> % first :version)))]
    (every?
     #(let [initial (->> %
                         (get identities)
                         (meta)
                         ::version)
            current (get versions %)]
        (= initial current))
     ids)))

(defn- delete-all [conn mappers identities]
  (let [groups (->> identities
                    (vals)
                    (filter need-delete?)
                    (map deref)
                    (group-by class)
                    (ext/map-keys #(get mappers %))
                    (ext/map-vals #(map aggregate/id %)))]
    (doseq [[manager ids] groups]
      (delete manager conn ids))))

(defn- insert-all [conn mappers identities]
  (let [groups (->> identities
                    (vals)
                    (filter need-insert?)
                    (map deref)
                    (group-by class)
                    (ext/map-keys #(get mappers %)))]
    (doseq [[manager aggregates] groups]
      (insert manager conn aggregates))))

(defn- commit [tx mappers]
  (let [data-source (:data-source tx)
        identities  @(:identity-map tx)]
    (with-open [conn (jdbc/connection data-source)]
      (jdbc/atomic conn
                   (when (lock-all conn mappers identities)
                     (delete-all conn mappers identities)
                     (insert-all conn mappers identities)
                     true)))))

(defn- timestamp []
  (inst-ms (Instant/now)))

(deftype Storage [data-source mappers opts]
  storage/Storage
  (wrap-tx [this body]
    (let [soft-timeout (get opts :soft-timeout-ms 500)]
      (loop [stop-after (+ (timestamp) soft-timeout)]
        (let [tx       (build-tx data-source mappers)
              res      (body tx)
              success? (commit tx mappers)]
          (cond
            success?                   res
            (< (timestamp) stop-after) (recur stop-after)
            :else                      (throw (TimeoutException. "Can't retry transaction"))))))))


(s/fdef binding-map
  :args (s/cat :data-source any?
               :mappers (s/map-of class? ::mapper)
               :opts (s/? map?))
  :ret map?)

(defn binding-map
  ([data-source mappers]
   (binding-map data-source mappers {}))
  ([data-source mappers opts]
   {#'storage/*storage* (Storage. data-source mappers opts)}))
