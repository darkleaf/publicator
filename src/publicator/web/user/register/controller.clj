(ns publicator.web.user.register.controller
  (:require
   [publicator.interactors.user.register :as interactor]
   [publicator.web.interactor-response :as interactor-resp]
   [publicator.web.problem-presenter :as problem-presenter]
   [publicator.web.user.register.view :as view]
   [publicator.ring.helpers :refer [path-for]]
   [form-ujs.ring]))

(defn form [req]
  (let [resp (interactor/initial-params)]
    (interactor-resp/handle resp)))

(defn handler [req]
  (let [params (form-ujs.ring/request->data req)
        resp   (interactor/process params)]
    (interactor-resp/handle resp)))

(defmethod interactor-resp/handle ::interactor/initial-params [resp]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (view/render-form ::interactor/params
                              (:initial-params resp))})

(defmethod interactor-resp/handle ::interactor/processed [resp]
  (form-ujs.ring/successful-response (path-for :root)))

(defmethod interactor-resp/handle ::interactor/already-registered [resp]
  (form-ujs.ring/failure-response (view/already-registered)))

(derive ::interactor/already-logged-in ::interactor-resp/forbidden)
(derive ::interactor/invalid-params ::interactor-resp/invalid-params)

(defn routes []
  [[:get "/registration" #'form :user.register/form]
   [:post "/registration" #'handler :user.register/handler]])
