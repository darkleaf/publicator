(ns publicator.web.post.update.controller
  (:require
   [form-ujs.spec]
   [publicator.interactors.post.update :as interactor]
   [publicator.web
    [interactor-response :as interactor-resp]
    [transit :as t]
    [problem-presenter :as problem-presenter]]
   [publicator.web.post.update
    [view :as view]]
   [io.pedestal.http.route :as route]))

(defn form [req]
  (let [id (-> req :path-params :id bigint)
        resp (interactor/initial-params id)]
    (interactor-resp/handle resp)))

(defn handler [req]
  (let [id (-> req :path-params :id bigint)
        params (:transit-params req)
        resp   (interactor/process id params)]
    (interactor-resp/handle resp)))

(defmethod interactor-resp/handle ::interactor/initial-params [resp]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (view/render ::interactor/params
                         (:initial-params resp)
                         {})})

(defmethod interactor-resp/handle ::interactor/processed [resp]
  {:status  200
   :headers {"Location" (route/url-for :root)}})

(defmethod interactor-resp/handle ::interactor/invalid-params [resp]
  {:status  422
   :headers {"Content-Type" "application/transit+json"}
   :body    (->> resp
                 :explain-data
                 (form-ujs.spec/errors problem-presenter/present)
                 t/write)})

(derive ::interactor/logged-out ::interactor-resp/forbidden)
(derive ::interactor/not-authorized ::interactor-resp/forbidden)
(derive ::interactor/not-found ::interactor-resp/not-found)

(defn routes []
  #{["/posts/:id/edit" :get #'form :route-name :post.update/form]
    ["/posts/:id" :patch #'handler :route-name :post.update/handler]})
