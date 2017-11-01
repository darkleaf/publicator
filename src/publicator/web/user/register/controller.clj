(ns publicator.web.user.register.controller
  (:require
   [form-ujs.spec]
   [publicator.interactors.user.register :as interactor]
   [publicator.transit :as transit]
   [publicator.web.interactor-response :as interactor-resp]
   [publicator.web.problem-presenter :as problem-presenter]
   [publicator.web.user.register.view :as view]
   [publicator.web.user.register.messages :as messages]
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

(defmethod interactor-resp/handle ::interactor/already-registered [resp]
  {:status  422
   :headers {"Content-Type" "application/transit+json"}
   :body    (transit/read-str messages/already-registered)})

(derive ::interactor/already-logged-in ::interactor-resp/forbidden)
(derive ::interactor/invalid-params ::interactor-resp/invalid-params)

(defn routes []
  [[:get "/registration" #'form :user.register/form]
   [:post "/registration" #'handler :user.register/handler]])
