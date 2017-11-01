(ns publicator.web.post.create.controller
  (:require
   [publicator.interactors.post.create :as interactor]
   [publicator.web.interactor-response :as interactor-resp]
   [publicator.web.post.create.view :as view]
   [publicator.ring.helpers :refer [path-for]]
   [form-ujs.ring]))

(defn form [req]
  (let [resp (interactor/initial-params)]
    (interactor-resp/handle resp)))

(defn handler [req]
  (let [params (form-ujs.ring/request->data req)
        resp   (interactor/process params)]
    (interactor-resp/handle resp)))

(defmethod interactor-resp/handle ::interactor/initial-params [resp]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (view/render-form ::interactor/params
                              (:initial-params resp))})

(defmethod interactor-resp/handle ::interactor/processed [resp]
  (form-ujs.ring/successful-response (path-for :root)))

(derive ::interactor/invalid-params ::interactor-resp/invalid-params)
(derive ::interactor/logged-out ::interactor-resp/forbidden)

(defn routes []
  [[:get "/posts-new" #'form :post.create/form]
   [:post "/posts" #'handler :post.create/handler]])
