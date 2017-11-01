(ns publicator.web.post.create.controller
  (:require
   [form-ujs.spec]
   [publicator.interactors.post.create :as interactor]
   [publicator.transit :as transit]
   [publicator.web
    [interactor-response :as interactor-resp]
    [problem-presenter :as problem-presenter]]
   [publicator.web.post.create
    [view :as view]]
   [publicator.ring.helpers :refer [path-for]]))

(defn form [req]
  (let [resp (interactor/initial-params)]
    (interactor-resp/handle resp)))

(defn handler [req]
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
   :headers {"Location" (path-for :root)}})

(derive ::interactor/invalid-params ::interactor-resp/invalid-params)
(derive ::interactor/logged-out ::interactor-resp/forbidden)

(defn routes []
  [[:get "/posts-new" #'form :post.create/form]
   [:post "/posts" #'handler :post.create/handler]])
