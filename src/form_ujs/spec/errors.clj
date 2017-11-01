(ns form-ujs.spec.errors
  (:require
   [form-ujs.errors :as errors]
   [clojure.spec.alpha :as s]))

(defn- add-error [errors presented]
  (let [[in message] presented]
    (errors/add-error errors in message)))

(defn errors [problem-presenter explain-data]
  "problem-presenter :: spec-problem -> [in, message]
  in :: []
  message :: string?"
  (let [problems  (::s/problems explain-data)
        presented (map problem-presenter problems)]
    (reduce add-error {} presented)))
