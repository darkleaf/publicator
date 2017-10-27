(ns publicator.web.user.log-out.controller
  (:require
   [form-ujs.spec]
   [publicator.interactors.user.log-out :as interactor]
   [publicator.web
    [interactor-response :as interactor-resp]]
   [io.pedestal.http.route :as route]))

(defn handler [req]
  (let [resp   (interactor/process)]
    (interactor-resp/handle resp)))

(defmethod interactor-resp/handle ::interactor/processed [resp]
  {:status  302
   :headers {"Location" (route/url-for :root)}})

(derive ::interactor/already-logged-out ::interactor-resp/forbidden)

(defn routes []
  #{["/log-out" :post #'handler :route-name :user-log-out]})
