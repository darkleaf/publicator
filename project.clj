(defproject publicator "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha19"]
                 [buddy/buddy-hashers "1.2.0"]
                 [better-cond "2.0.1-SNAPSHOT"]]
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.9.0"]]
                   :source-paths ["dev"]}})
