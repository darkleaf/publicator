(ns publicator.web.middlewares.bindings)

(defn wrap-bindings [handler binding-map]
  (fn [req]
    (with-bindings binding-map
      (handler req))))
