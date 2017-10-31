(ns publicator.transit
  (:require
   [cognitect.transit :as t])
  (:import
   [java.io ByteArrayInputStream ByteArrayOutputStream]))

(defn write-str [data]
  (let [out    (ByteArrayOutputStream.)
        writer (t/writer out :json)
        _      (t/write writer data)]
    (.toString out)))

(defn read-stream [s]
  (let [reader (t/reader s :json)]
    (t/read reader)))

(defn read-str [s]
  (let [in (ByteArrayInputStream. (.getBytes s))]
    (read-stream in)))
