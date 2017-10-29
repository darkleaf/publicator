(ns publicator.web.pages.root.controller)

(defn handler [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "root"})

(defn routes []
  #{["/" :get #'handler :route-name :root]})
