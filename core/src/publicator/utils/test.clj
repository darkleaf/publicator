(ns publicator.utils.test
  (:require
   [clojure.string :as str]
   [clojure.test :as t]))

(defn- test-vars [ns-symbol]
  (require ns-symbol)
  (->> ns-symbol
       find-ns
       ns-publics
       (filter (fn [[var-name var]]
                 (str/starts-with? var-name "test-")))
       (map last)))

(defn run [ns-symbol each-fixture]
  (doseq [var (test-vars ns-symbol)]
    (t/testing var
      (each-fixture var))))
