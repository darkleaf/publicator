(ns publicator.web.controllers.routes
  (:require
   [clojure.set :as set]
   [clojure.tools.namespace.find :as ctn.find]
   [clojure.java.io :as io]))

(defn- subdirs [dir]
  (->> (io/file dir)
       .list
       (map #(io/file dir %))
       (filter #(.isDirectory %))))

(def routes
  (let [dirs       (subdirs "src/publicator/web/controllers")
        namespaces (mapcat ctn.find/find-namespaces-in-dir dirs)
        routes     (for [ns namespaces]
                     (do
                       (require ns)
                       @(resolve (symbol (str ns) "routes"))))]
    (apply set/union routes)))
