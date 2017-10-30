(ns publicator.web.problem-presenter
  (:require
   [clojure.spec.alpha :as s]
   [akar.syntax :refer [match]]
   [akar.patterns :refer :all]))

;; https://github.com/missingfaktor/akar/issues/20
(defmacro fn-silent-> [& xs]
  `(fn [x#]
     (try
       (-> x# ~@xs)
       (catch RuntimeException _# nil))))

;;todo wait akar 0.2.0
(defn- !required-key [problem]
  (comment
    {:path [], :val {}, :in []
     :pred (clojure.core/fn [%] (clojure.core/contains? % :SOME/K))
     :via [:SOME/SPEC-1 :SOME/SPEC-2]})
  (match problem
         {:via (:view (fn-silent-> peek s/form first) [(!constant `s/keys)])
          :pred (:and (:view (fn-silent-> last first) [(!constant `contains?)])
                      (:view (fn-silent-> last last) k))
          :in in}
         [(conj in k)]
         :_ nil))

(defn- !min-max-regex [char-pattern]
  (fn [problem]
    (comment
      {:path [:password], :val "1234", :in [:password]
       :pred (clojure.core/fn [%]
               (clojure.core/re-matches #".{8,255}" %)),
       :via []})
    (match problem
           {:pred (:and
                   (:view (fn-silent-> last first)
                          [(!constant `re-matches)])
                   (:view (fn-silent-> last second str)
                          [(!regex (re-pattern
                                    (str "\\A"
                                         char-pattern
                                         "\\{(\\d+),(\\d+)\\}\\z")))
                           (:view bigint r-min)
                           (:view bigint r-max)]))
            :in in}
           [in r-min r-max]
           :_ nil)))

(defn present [problem]
  (match problem
         [!required-key in]
         [in "Обязательное"]

         [(!min-max-regex #"\.") in r-min r-max]
         [in (str "Кол-во символов от " r-min " до " r-max)]

         [(!min-max-regex #"\\w") in r-min r-max]
         [in (str "Кол-во латинских букв и цифр от " r-min " до " r-max)]

         {:in in}
         [in "Неопознанная ошибка, обратитесь к администратору"]))
