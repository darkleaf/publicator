(ns publicator.web.user.log-in.controller
  (:require
   [form-ujs.spec]
   [publicator.interactors.user.log-in :as interactor]
   [publicator.web
    [interactor-response :as interactor-resp]
    [transit :as t]
    [problem-presenter :as problem-presenter]]
   [publicator.web.user.log-in
    [view :as view]
    [messages :as messages]]
   [io.pedestal.http.route :as route]))

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
                         messages/no-errors)})

(defmethod interactor-resp/handle ::interactor/processed [resp]
  {:status  200
   :headers {"Location" (route/url-for :root)}})

(defmethod interactor-resp/handle ::interactor/authentication-failed [resp]
  {:status  422
   :headers {"Content-Type" "application/transit+json"}
   :body    (t/write messages/authentication-failed)})

(defmethod interactor-resp/handle ::interactor/invalid-params [resp]
  {:status  422
   :headers {"Content-Type" "application/transit+json"}
   :body    (->> resp
                 :explain-data
                 (form-ujs.spec/errors problem-presenter/present)
                 t/write)})

(derive ::interactor/already-logged-in ::interactor-resp/forbidden)

(defn routes []
  #{["/log-in" :get #'form :route-name :user.log-in/form]
    ["/log-in" :post #'handler :route-name :user.log-in/handler]})
