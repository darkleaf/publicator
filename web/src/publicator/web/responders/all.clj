(ns publicator.web.responders.all
  (:require
   [clojure.tools.namespace.find :as ctn.find])
  (:import
   [java.io File FilenameFilter]))

(defn- subdirs [dir]
  (->> (File. dir)
       .list
       (map #(File. dir %))
       (filter #(.isDirectory %))))

(let [dirs       (subdirs "src/publicator/web/responders")
      namespaces (mapcat ctn.find/find-namespaces-in-dir dirs)]
  (doseq [ns namespaces]
    (require ns)))
