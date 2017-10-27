(ns form-ujs.html
  (:require
   [cognitect.transit :as t]
   [hiccup.core :refer [html]])
  (:import
   [java.io ByteArrayInputStream ByteArrayOutputStream]))

(def transit-opts {})

(defn- transit-write [data]
  (let [out (ByteArrayOutputStream.)
        writer (t/writer out :json transit-opts)
        _ (t/write writer data)]
    (.toString out)))

(defn form [description data errors]
  (let [id (gensym "form-ujs")]
    (html
     [:div
      [:div {:data-form-ujs id}]
      [:script
       {:id (str id "-description")
        :type "application/transit+json"}
       (transit-write description)]
      [:script
       {:id (str id "-data")
        :type "application/transit+json"}
       (transit-write data)]])))
