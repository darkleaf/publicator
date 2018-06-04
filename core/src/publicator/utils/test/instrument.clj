(ns publicator.utils.test.instrument
  (:require
   [orchestra.spec.test :as st]))

(defn fixture [f]
  ;; Для параллельных тестов можно попробовать locking.
  ;; Но есть подозрение, что это не заработает.
  ;; (locking st/instrument
  ;;   (st/instrument))
  (st/instrument)
  (f))
