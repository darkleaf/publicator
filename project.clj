(defproject publicator "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-RC1"]
                 [better-cond "2.0.1-SNAPSHOT"]
                 [com.stuartsierra/component "0.3.2"]


                 [ring/ring-core "1.6.2"]
                 [ring/ring-jetty-adapter "1.6.2"]
                 [functionalbytes/sibiro "0.1.5"]
                 [metosin/ring-http-response "0.9.0"]

                 [hiccup "1.0.5"]
                 [com.cognitect/transit-clj "0.8.300"]

                 ;; future impl
                 ;; [buddy/buddy-hashers "1.3.0"]

                 ;; overrides
                 [org.clojure/core.async "0.3.443"]]
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.9.0"]
                                  [ring/ring-mock "0.3.1"]]
                   :source-paths ["dev"]}}
  :local-repo "local-m2")
