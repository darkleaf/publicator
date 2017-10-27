(ns publicator.web.post.show.controller
  (:require
   [form-ujs.spec]
   [publicator.interactors.post.show :as interactor]
   [publicator.web
    [interactor-response :as interactor-resp]]
   [publicator.web.post.show
    [view :as view]]))

(defn handler [req]
  (let [id (-> req :path-params :id bigint)
        resp (interactor/process id)]
    (interactor-resp/handle resp)))

(defmethod interactor-resp/handle ::interactor/processed [resp]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body (view/render (:post resp))})

(derive ::interactor/not-found ::interactor-resp/not-found)

(defn routes []
  #{["/posts/:id" :get #'handler :route-name :post.show/handler]})
