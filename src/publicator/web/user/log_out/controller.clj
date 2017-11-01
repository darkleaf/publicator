(ns publicator.web.user.log-out.controller
  (:require
   [publicator.interactors.user.log-out :as interactor]
   [publicator.web.interactor-response :as interactor-resp]
   [publicator.ring.helpers :refer [path-for]]))

(defn handler [req]
  (let [resp   (interactor/process)]
    (interactor-resp/handle resp)))

(defmethod interactor-resp/handle ::interactor/processed [resp]
  {:status  302
   :headers {"Location" (path-for :root)}})

(derive ::interactor/already-logged-out ::interactor-resp/forbidden)

(defn routes []
  [[:post "/log-out" #'handler :user.log-out/handler]])
