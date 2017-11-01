(ns publicator.web.post.list.controller
  (:require
   [publicator.interactors.post.list :as interactor]
   [publicator.web
    [interactor-response :as interactor-resp]]
   [publicator.web.post.list
    [view :as view]]))

(defn handler [req]
  (let [resp (interactor/process)]
    (interactor-resp/handle resp)))

(defmethod interactor-resp/handle ::interactor/processed [resp]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body (view/render (:posts resp))})


(defn routes []
  [[:get "/posts" #'handler :post.list/handler]])
