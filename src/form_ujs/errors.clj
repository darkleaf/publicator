(ns form-ujs.errors)

(defn blank [] {})

(defn add-error [errors path message]
  (let [path (conj path :form-ujs/errors)]
    (update-in errors path #(conj (vec %) message))))

(defn error
  ([message]
   (error [] message))
  ([path message]
   (add-error (blank) path message)))
