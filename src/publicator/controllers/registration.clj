(ns publicator.controllers.registration
  (:require
   [publicator.interactors.user.register :as register]
   [publicator.views.registration.new :as views.new]))

(defn new [req]
  (let [impl (:impl req)]
    #_(prn impl))

  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (views.new/render nil)})

(defn create [req]
  (let [impl   (:impl req)
        params (:transit-params req)]
    (prn (register/call impl params)))
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "ok"})

(defn routes []
  #{["/registration/new" :get new :route-name :new-registration]
    ["/registration" :post create :route-name :create-registration]})
