(ns publicator.web.pages.root.controller)

(defn root [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "root"})

(defn routes []
  #{["/" :get root :route-name :root]})
