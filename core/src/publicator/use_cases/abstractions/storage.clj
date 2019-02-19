(ns publicator.use-cases.abstractions.storage)

(declare ^{:dynamic true, :arglists '([func-from-t])}
         atomic-apply

         ^{:dynamic true, :arglists '([t ids])}
         get-many

         ^{:dynamic true, :arglists '([t state])}
         create)

(defmacro atomic
  {:style/indent [1 [[:defn]] :form]}
  [t-name & body]
  `(atomic-apply (fn [~t-name] ~@body)))

(defn get-one [t id]
  (let [res (get-many t [id])]
    (get res id)))

;;(defn preload [t ids]) ;; alias




;; (defn atomic-get-one [id]
;;   (atomic t
;;     (when-let [x (get-one t id)]
;;       @x)))

;; (defn atomic-get-many [ids]
;;   (atomic t
;;     (->> ids
;;          (get-many t)
;;          (ext/map-vals deref))))

;; (defn atomic-create [state]
;;   (atomic t
;;     @(create t state)))

;; (defn atomic-alter [state f & args]
;;   (atomic t
;;     (when-let [x (get-one t (aggregate/id state))]
;;       (dosync
;;        (apply alter x f args)))))
