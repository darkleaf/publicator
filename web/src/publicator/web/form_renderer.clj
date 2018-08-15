(ns publicator.web.form-renderer
  (:require
   [cljstache.core :as mustache]
   [publicator.web.transit :as transit]))

(defn render [form]
  (mustache/render "<div data-form-ujs=\"{{ data }}\" />", {:data (transit/write form)}))
