(ns publicator.use-cases.abstractions.storage)

(declare ^{:dynamic true, :arglists '([func])}
         *transaction*

         ^{:dynamic true, :arglists '([state])}
         *create*

         ^{:dynamic true, :arglists '([type ids])}
         *preload*

         ^{:dynamic true, :arglists '([type id])}
         *get*)

(defmacro transaction [& body]
  `(*transaction* #(do ~@body)))
