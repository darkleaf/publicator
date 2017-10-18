(ns form-ujs.spec
  (:require
   [clojure.spec.alpha :as s]))

(defn- add-error [errors presented]
  (let [[in message] presented
        in           (conj in :form-ujs/errors)]
    (update-in errors in #(conj (vec %) message))))

(defn errors [problem-presenter explain-data]
  "problem-presenter :: spec-problem -> [in, message]
  in :: []
  message :: string?"
  (let [problems  (::s/problems explain-data)
        presented (map problem-presenter problems)]
    (reduce add-error {} presented)))
