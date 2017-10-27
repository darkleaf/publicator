(ns publicator.web.post.destroy.controller
  (:require
   [form-ujs.spec]
   [publicator.interactors.post.destroy :as interactor]
   [publicator.web
    [interactor-response :as interactor-resp]]
   [io.pedestal.http.route :as route]))

(defn handler [req]
  (let [id   (-> req :path-params :id bigint)
        resp (interactor/process id)]
    (interactor-resp/handle resp)))

(defmethod interactor-resp/handle ::interactor/processed [resp]
  {:status  302
   :headers {"Location" (route/url-for :root)}})

(derive ::interactor/logged-out ::interactor-resp/forbidden)
(derive ::interactor/not-authorized ::interactor-resp/forbidden)
(derive ::interactor/not-found ::interactor-resp/not-found)

(defn routes []
  #{["/posts/:id" :delete #'handler :route-name :post-destroy]})
