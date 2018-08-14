(ns publicator.web.responders.post.update-test
  (:require
   [publicator.utils.test.instrument :as instrument]
   [publicator.web.responders.post.update :as sut]
   [publicator.web.responders.base :as base]
   [publicator.use-cases.test.factories :as factories]
   [publicator.use-cases.interactors.post.update :as interactor]
   [ring.util.http-predicates :as http-predicates]
   [clojure.test :as t]
   [clojure.spec.alpha :as s]
   [clojure.set :as set]))

(t/use-fixtures :once instrument/fixture)

(defn is-all-types-implemented [sym]
  (let [[_ & pairs] (-> sym s/get-spec :ret s/describe)
        specs       (keep-indexed
                     (fn [idx item] (if (odd? idx) item))
                     pairs)
        implemented (-> base/->resp methods keys)]
    (doseq [spec specs]
      (t/is (some #(isa? spec %) implemented) (str spec " not implemented")))))

(t/deftest all-implemented
  (is-all-types-implemented `interactor/initial-params)
  (is-all-types-implemented `interactor/process))

(t/deftest initial-params
  (let [result (factories/gen ::interactor/initial-params)
        args   [1]
        resp   (base/->resp result args)]
    (t/is (http-predicates/ok? resp))))

(t/deftest invalid-params
  (let [result [::interactor/invalid-params (s/explain-data ::interactor/params {})]
        args   [1]
        resp   (base/->resp result args)]
    (t/is (http-predicates/unprocessable-entity? resp))))

(t/deftest processed
  (let [result (factories/gen ::interactor/processed)
        args   [1]
        resp   (base/->resp result args)]
    (t/is (http-predicates/created? resp))))
