(ns publicator.web.responders.shared-testing
  (:require
   [publicator.web.responders.base :as responders.base]
   [clojure.spec.alpha :as s]
   [clojure.test :as t]))

(defn all-responders-are-implemented [sym]
  (t/testing sym
    (let [[_ & pairs] (-> sym s/get-spec :ret s/describe)
          specs       (keep-indexed
                       (fn [idx item] (if (odd? idx) item))
                       pairs)
          implemented (-> responders.base/result->resp methods keys)]
      (doseq [spec specs]
        (t/testing spec
          (t/is (some #(isa? spec %) implemented) "not implemented"))))))
