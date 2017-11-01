(ns form-ujs.html
  (:require
   [form-ujs.transit :as t]
   [form-ujs.errors :as errors]))

(defn form
  ([description data]
   (form description data (errors/blank)))
  ([description data errors]
   (let [id (gensym "form-ujs")]
     [:div
      [:div {:data-form-ujs id}]
      [:script
       {:id (str id "-description")
        :type "application/transit+json"}
       (t/write-str description)]
      [:script
       {:id (str id "-data")
        :type "application/transit+json"}
       (t/write-str data)]
      [:script
       {:id (str id "-errors")
        :type "application/transit+json"}
       (t/write-str errors)]])))
