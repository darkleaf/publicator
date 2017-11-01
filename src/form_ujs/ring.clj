(ns form-ujs.ring
  (:require
   [form-ujs.transit :as t]))

(defn request->data [req]
  {:pre [(= "application/transit+json"
            (:content-type req))]}
  (t/read-stream (:body req)))

(defn data->request [request-method uri data]
  {:request-method request-method
   :uri            uri
   :body           (t/write-stream data)
   :headers        {"Content-Type" "application/transit+json"}
   :content-type   "application/transit+json"})

(defn successful-response [redirect-url]
  {:status  200
   :headers {"Location" redirect-url}})

(defn failure-response [errors]
  {:status  422
   :headers {"Content-Type" "application/transit+json"}
   :body    (t/write-stream errors)})

(defn successful-response? [resp]
  (and
   (= 200 (:status resp))
   (some? (get-in resp [:headers "Location"]))))


(defn failure-response? [resp])
