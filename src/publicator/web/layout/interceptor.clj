(ns publicator.web.layout.interceptor
  (:require
   [publicator.web.layout
    [view :as view]]))

(def layout
  {:leave (fn [context]
            (if (= (get-in context [:response :headers "Content-Type"]) "text/html")
              (update-in context [:response :body] view/render)
              context))})
