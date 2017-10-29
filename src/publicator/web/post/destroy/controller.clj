(ns publicator.web.post.destroy.controller
  (:require
   [form-ujs.spec]
   [publicator.interactors.post.destroy :as interactor]
   [publicator.web
    [interactor-response :as interactor-resp]]
   [publicator.ring.helpers :refer [path-for]]))

(defn handler [req]
  (let [id   (-> req :route-params :id bigint)
        resp (interactor/process id)]
    (interactor-resp/handle resp)))

(defmethod interactor-resp/handle ::interactor/processed [resp]
  {:status  302
   :headers {"Location" (path-for :root)}})

(derive ::interactor/logged-out ::interactor-resp/forbidden)
(derive ::interactor/not-authorized ::interactor-resp/forbidden)
(derive ::interactor/not-found ::interactor-resp/not-found)

(defn routes []
  [[:delete "/posts/:id" #'handler :post.destroy/handler]])
