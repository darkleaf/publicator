(ns publicator.persistence.types
  (:require
   [next.jdbc.prepare :as prepare]
   [next.jdbc.result-set :as result-set]
   [next.jdbc.date-time :as date-time]
   [publicator.core.domain.aggregate :as agg]
   [medley.core :as m])
  (:import
   [java.sql Array PreparedStatement]))

(set! *warn-on-reflection* true)

(date-time/read-as-instant)

(extend-protocol result-set/ReadableColumn
  Array
  (read-column-by-label [^Array v _]    (vec (.getArray v)))
  (read-column-by-index [^Array v _ _]  (vec (.getArray v))))

(extend-protocol prepare/SettableParameter
  clojure.lang.IPersistentVector
  (set-parameter [v ^PreparedStatement s i]
    (.setObject s i (into-array v))))

(let [enum {:persistence.value/read  keyword
            :persistence.value/write name}]
  (swap! agg/schema m/deep-merge
         {:user/state                    enum
          :author.achivement/kind        enum
          :publication/type              enum
          :publication/state             enum
          :publication.translation/state enum}))
