(ns publicator.web.helpers
  (:require
   [hiccup.core :refer [html]]))

(defn link-to [name url & {:as html-opts}]
  (html
   [:a
    (merge html-opts {:href url})
    name]))

(defn action [name url method & {:as html-opts}]
  (html
   [:form {:action url, :method method}
    [:button
     (merge html-opts {:type :submit})
     name]]))
