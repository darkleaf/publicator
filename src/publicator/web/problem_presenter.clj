(ns publicator.web.problem-presenter
  (:require
   [clojure.spec.alpha :as s]
   [better-cond.core :as b]))

(defmacro try-> [x & xs]
  `(try
     (-> ~x ~@xs)
     (catch RuntimeException _# nil)))

(comment
  {:path [], :val {}, :in []
   :pred (clojure.core/fn [%] (clojure.core/contains? % :SOME/K))
   :via [:SOME/SPEC-1 :SOME/SPEC-2]})

(b/defnc- !required-key [problem]
  :when-let [_  (= `s/keys (try-> problem :via peek s/form first))
             _  (= `contains? (try-> problem :pred last first))
             k  (try-> problem :pred last last)
             in (try-> problem :in vec)]
  [(conj in k)])


(comment
  {:path [:password], :val "1234", :in [:password]
   :pred (clojure.core/fn [%]
           (clojure.core/re-matches #".{8,255}" %)),
   :via []})

(b/defnc !min-max-regex [problem char-pattern]
  :when-let [pattern (re-pattern (str "\\A" char-pattern "\\{(\\d+),(\\d+)\\}\\z"))
             _       (= `re-matches (try-> problem :pred last first))
             regex   (try-> problem :pred last second str)
             matches (re-matches pattern regex)
             r-min   (try-> matches (get 1) bigint)
             r-max   (try-> matches (get 2) bigint)
             in      (try-> problem :in vec)]
  [in r-min r-max])


(b/defnc present [problem]
  :let [[in :as res] (!required-key problem)]
  (some? res) [in "Обязательное"]

  :let [[in r-min r-max :as res] (!min-max-regex problem #"\.")]
  (some? res) [in (str "Кол-во символов от " r-min " до " r-max)]

  :let [[in r-min r-max :as res] (!min-max-regex problem #"\\w")]
  (some? res) [in (str "Кол-во латинских букв и цифр от " r-min " до " r-max)]

  :let [in (:in problem)]
  [in "Неопознанная ошибка, обратитесь к администратору"])
