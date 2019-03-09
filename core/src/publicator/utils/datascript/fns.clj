(ns publicator.utils.datascript.fns
  (:require
   [datascript.core :as d]))

(defn update-all [db attribute f & args]
  (let [e-v     (d/q '{:find  [?e ?v]
                       :in    [$ ?a]
                       :where [[?e ?a ?v]]}
                     db attribute)
        retract (for [[e v] e-v]
                  [:db/retract e attribute v])
        add     (for [[e v] e-v]
                  [:db/add e attribute (apply f v args)])]
    (concat retract add)))
