(ns publicator.web.user.log-in.controller
  (:require
   [publicator.interactors.user.log-in :as interactor]
   [publicator.web.interactor-response :as interactor-resp]
   [publicator.web.user.log-in.view :as view]
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
   :body    (view/render-from ::interactor/params
                              (:initial-params resp))})

(defmethod interactor-resp/handle ::interactor/processed [resp]
  (form-ujs.ring/successful-response (path-for :root)))

(defmethod interactor-resp/handle ::interactor/authentication-failed [resp]
  (form-ujs.ring/failure-response (view/authentication-failed)))

(derive ::interactor/already-logged-in ::interactor-resp/forbidden)
(derive ::interactor/invalid-params ::interactor-resp/invalid-params)

(defn routes []
  [[:get "/log-in" #'form :user.log-in/form]
   [:post "/log-in" #'handler :user.log-in/handler]])
