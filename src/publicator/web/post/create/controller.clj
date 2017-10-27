(ns publicator.web.post.create.controller
  (:require
   [form-ujs.spec]
   [publicator.interactors.post.create :as interactor]
   [publicator.web
    [interactor-response :as interactor-resp]
    [transit :as t]
    [problem-presenter :as problem-presenter]]
   [publicator.web.post.create
    [view :as view]]
   [io.pedestal.http.route :as route]))

(defn form [req]
  (let [resp (interactor/initial-params)]
    (interactor-resp/handle resp)))

(defn form-handler [req]
  (let [params (:transit-params req)
        resp   (interactor/process params)]
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

(defn routes []
  #{["/posts-new" :get #'form :route-name :post-create-form]
    ["/posts" :post #'form-handler :route-name :post-create]})
