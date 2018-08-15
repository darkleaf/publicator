(ns publicator.web.responders.all
  (:require
   [clojure.tools.namespace.find :as ctn.find]
   [clojure.java.io :as io]))

(defn- subdirs [dir]
  (->> (io/file dir)
       .list
       (map #(io/file dir %))
       (filter #(.isDirectory %))))

(let [dirs       (subdirs "src/publicator/web/responders")
      namespaces (mapcat ctn.find/find-namespaces-in-dir dirs)]
  (doseq [ns namespaces]
    (require ns)))
