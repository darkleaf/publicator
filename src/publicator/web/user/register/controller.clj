(ns publicator.web.user.register.controller
  (:require
   [form-ujs.spec]
   [publicator.interactors.user.register :as interactor]
   [publicator.web
    [interactor-response :as interactor-resp]
    [transit :as t]
    [problem-presenter :as problem-presenter]]
   [publicator.web.user.register
    [view :as view]
    [messages :as messages]]
   [io.pedestal.http.route :as route]))

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
   :body    (view/render ::interactor/params
                         (:initial-params resp)
                         messages/no-errors)})

(defmethod interactor-resp/handle ::interactor/processed [resp]
  {:status  200
   :headers {"Location" (route/url-for :root)}})

(defmethod interactor-resp/handle ::interactor/already-registered [resp]
  {:status  422
   :headers {"Content-Type" "application/transit+json"}
   :body    (t/write messages/already-registered)})

(defmethod interactor-resp/handle ::interactor/invalid-params [resp]
  {:status  422
   :headers {"Content-Type" "application/transit+json"}
   :body    (->> resp
                 :explain-data
                 (form-ujs.spec/errors problem-presenter/present)
                 t/write)})

(derive ::interactor/already-logged-in ::interactor-resp/forbidden)

(defn routes []
  #{["/registration" :get form :route-name :user-register-form]
    ["/registration" :post form-handler :route-name :user-register]})
