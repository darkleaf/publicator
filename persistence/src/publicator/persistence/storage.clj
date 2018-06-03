(ns publicator.persistence.storage
  (:require
   [jdbc.core :as jdbc]
   [publicator.use-cases.abstractions.storage :as storage]
   [publicator.domain.abstractions.aggregate :as aggregate]
   [publicator.domain.identity :as identity]
   [publicator.utils.ext :as ext]
   [clojure.spec.alpha :as s])
  (:import
   [java.util.concurrent TimeoutException]
   [java.time Instant]))

(defprotocol Mapper
  (-lock [this conn ids])
  (-select [this conn ids])
  (-insert [this conn states])
  (-delete [this conn ids]))

(s/def ::mapper #(satisfies? Mapper %))

(defn lock [this conn ids]
  (if (empty? ids)
    []
    (-lock this conn ids)))

(defn select [this conn ids]
  (if (empty? ids)
    []
    (-select this conn ids)))

(defn insert [this conn states]
  (if (empty? states)
    nil
    (-insert this conn states)))

(defn delete [this conn ids]
  (if (empty? ids)
    nil
    (-delete this conn ids)))

(deftype Transaction [data-source mappers identity-map]
  storage/Transaction
  (get-many [this ids]
    (with-open [conn (jdbc/connection data-source)]
      (let [ids-for-select (remove #(contains? @identity-map %) ids)
            selected       (->> mappers
                                (vals)
                                (mapcat #(select % conn ids-for-select))
                                (map (fn [[state version]]
                                       (let [istate (identity/build state)]
                                         (alter-meta! istate assoc
                                                      ::version version
                                                      ::initial state)
                                         istate)))
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
    (doseq [[manager states] groups]
      (insert manager conn states))))

(defn- commit [tx mappers]
  (let [data-source (.-data-source tx)
        identities  @(.-identity-map tx)]
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
