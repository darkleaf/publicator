(ns publicator.web.post.update.controller
  (:require
   [publicator.interactors.post.update :as interactor]
   [publicator.web.interactor-response :as interactor-resp]
   [publicator.web.post.update.view :as view]
   [publicator.ring.helpers :refer [path-for]]
   [form-ujs.ring]))

(defn form [req]
  (let [id   (-> req :route-params :id bigint)
        resp (interactor/initial-params id)
        resp (assoc resp :id id)]
    (interactor-resp/handle resp)))

(defn handler [req]
  (let [id (-> req :route-params :id bigint)
        params (form-ujs.ring/request->data req)
        resp   (interactor/process id params)]
    (interactor-resp/handle resp)))

(defmethod interactor-resp/handle ::interactor/initial-params [resp]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (view/render-form (:id resp)
                              ::interactor/params
                              (:initial-params resp))})

(defmethod interactor-resp/handle ::interactor/processed [resp]
  (form-ujs.ring/successful-response (path-for :root)))

(derive ::interactor/logged-out ::interactor-resp/forbidden)
(derive ::interactor/not-authorized ::interactor-resp/forbidden)
(derive ::interactor/not-found ::interactor-resp/not-found)
(derive ::interactor/invalid-params ::interactor-resp/invalid-params)

(defn routes []
  [[:get "/posts/:id/edit" #'form :post.update/form]
   [:patch "/posts/:id" #'handler :post.update/handler]])
