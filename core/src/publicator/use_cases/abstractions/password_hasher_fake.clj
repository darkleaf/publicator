(ns publicator.use-cases.abstractions.password-hasher-fake
  (:require
   [publicator.use-cases.abstractions.password-hasher :as password-hasher]
   [clojure.string :as str]))

(defn binding-map []
  {#'password-hasher/*derive* (fn [password] (str/reverse password))
   #'password-hasher/*check*  (fn [attempt encrypted] (= (str/reverse attempt)
                                                         encrypted))})
