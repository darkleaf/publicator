(ns publicator.controllers.user.register
  (:require
   [publicator.controllers.interactor-response :as interactor-resp]
   [publicator.interactors.user.register :as interactor]
   [publicator.views.registration.new :as views.new]))

(defn form [req]
  (let [ctx  (:interactor-ctx req)
        resp (interactor/initial-params ctx)]
    (interactor-resp/handle resp)))

(defn form-handler [req]
  (let [ctx    (:interactor-ctx req)
        params (:transit-params req)
        resp   (interactor/process ctx params)]
    (interactor-resp/handle resp)))

(defmethod interactor-resp/handle ::interactor/initial-params [resp]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (views.new/render nil)})

(defmethod interactor-resp/handle ::interactor/processed [resp]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "ok"})

(defmethod interactor-resp/handle ::interactor/already-registered [resp]
  {:status  403
   :headers {"Content-Type" "text/html"}
   :body    "already registered"})

(defmethod interactor-resp/handle ::interactor/invalid-params [resp]
  {:status  422
   :headers {"Content-Type" "text/html"}
   :body    "invalid params"})

(derive ::interactor/already-logged-in ::interactor-resp/forbidden)

(defn routes []
  #{["/registration" :get form :route-name :user-register-form]
    ["/registration" :post form-handler :route-name :user-register]})
