(ns form-ujs.errors)

(defn blank [] {})

(defn add-message [errors path message]
  (let [path (conj path :form-ujs/errors)]
    (update-in errors path #(conj (vec %) message))))
