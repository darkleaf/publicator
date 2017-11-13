(ns publicator.impl.id-generator-test
  (:require
   [clojure.test :as t]

   [jdbc.core :as jdbc]

   [publicator.impl.id-generator :as sut]
   [publicator.impl.test-data-source :refer [data-source]]
   [publicator.domain.abstractions.id-generator :as id-generator]))

(t/use-fixtures :each
  (fn [t]
    (with-open [conn (jdbc/connection data-source)]
      (jdbc/atomic
       conn
       (with-bindings (sut/binding-map (fn [f] (f conn)))
         (t)
         (jdbc/set-rollback! conn))))))

(t/deftest generate
    (t/is (pos-int? (id-generator/generate))))
