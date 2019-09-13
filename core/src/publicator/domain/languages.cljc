(ns publicator.domain.languages
  (:require
   [publicator.util :as u]))

(def languages #{:en :ru})
(def all-languages? #(u/same-items? % languages))
