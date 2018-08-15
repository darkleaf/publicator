(ns publicator.web.controllers.routes
  (:require
   [clojure.set :as set]
   [clojure.tools.namespace.find :as ctn.find])
  (:import
   [java.io File FilenameFilter]))

(defn- subdirs [dir]
  (->> (File. dir)
       .list
       (map #(File. dir %))
       (filter #(.isDirectory %))))

(def routes
  (let [dirs       (subdirs "src/publicator/web/controllers")
        namespaces (mapcat ctn.find/find-namespaces-in-dir dirs)
        routes     (for [ns namespaces]
                     (do
                       (require ns)
                       @(resolve (symbol (str ns) "routes"))))]
    (apply set/union routes)))
