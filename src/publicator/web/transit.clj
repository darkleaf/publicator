(ns publicator.web.transit
  (:require
   [cognitect.transit :as t])
  (:import
   [java.io ByteArrayInputStream ByteArrayOutputStream]))

(defn write [data]
  (let [out (ByteArrayOutputStream.)
        writer (t/writer out :json)
        _ (t/write writer data)]
    (.toString out)))
