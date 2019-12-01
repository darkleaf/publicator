(ns darkleaf.crypto.core
  (:require
   [darkleaf.crypto.hasher :as hasher]))

(def handlers
  {:hasher/derive #'hasher/derive
   :hasher/check  #'hasher/check})
