(ns form-ujs.transit
  (:require
   [cognitect.transit :as t])
  (:import
   [java.io ByteArrayInputStream ByteArrayOutputStream]))

(def opts {})

(defn write-stream [data]
  (let [out    (ByteArrayOutputStream.)
        writer (t/writer out :json opts)
        _      (t/write writer data)]
    (ByteArrayInputStream. (.toByteArray out))))

(defn write-str [data]
  (let [out    (ByteArrayOutputStream.)
        writer (t/writer out :json opts)
        _      (t/write writer data)]
    (.toString out)))

(defn read-stream [s]
  (let [reader (t/reader s :json opts)]
    (t/read reader)))

(defn read-str [s]
  (let [in (ByteArrayInputStream. (.getBytes s))]
    (read-stream in)))
