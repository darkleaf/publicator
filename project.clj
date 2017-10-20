(defproject publicator "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-beta2"]
                 [better-cond "2.0.1-SNAPSHOT"]
                 [com.stuartsierra/component "0.3.2"]
                 [io.pedestal/pedestal.service "0.5.2"]
                 [io.pedestal/pedestal.jetty "0.5.2"]
                 [org.slf4j/slf4j-simple "1.7.25"]
                 [hiccup "1.0.5"]
                 [com.cognitect/transit-clj "0.8.300"]
                 [akar "0.1.0"]

                 ;; future impl
                 ;; [buddy/buddy-hashers "1.3.0"]

                 ;; overrides
                 [org.clojure/core.async "0.3.443"]]
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.9.0"]]
                   :source-paths ["dev"]}}
  :local-repo "local-m2")
