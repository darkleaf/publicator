(ns publicator.web.user.log-in.controller
  (:require
   [form-ujs.spec]
   [publicator.interactors.user.log-in :as interactor]
   [publicator.transit :as transit]
   [publicator.web
    [interactor-response :as interactor-resp]
    [problem-presenter :as problem-presenter]]
   [publicator.web.user.log-in
    [view :as view]
    [messages :as messages]]
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
                         messages/no-errors)})

(defmethod interactor-resp/handle ::interactor/processed [resp]
  {:status  200
   :headers {"Location" (path-for :root)}})

(defmethod interactor-resp/handle ::interactor/authentication-failed [resp]
  {:status  422
   :headers {"Content-Type" "application/transit+json"}
   :body    (transit/write-str messages/authentication-failed)})

(derive ::interactor/already-logged-in ::interactor-resp/forbidden)
(derive ::interactor/invalid-params ::interactor-resp/invalid-params)

(defn routes []
  [[:get "/log-in" #'form :user.log-in/form]
   [:post "/log-in" #'handler :user.log-in/handler]])
