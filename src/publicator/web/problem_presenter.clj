(ns publicator.web.problem-presenter
  (:require
   [clojure.spec.alpha :as s]
   [form-ujs.errors :as errors]))

(defmacro when-some* [bindings & body]
  (if (empty? bindings)
    `(do ~@body)
    `(when-some [~@(take 2 bindings)]
       (when-some* [~@(drop 2 bindings)]
         ~@body))))

(comment
  {:path [], :val {}, :in []
   :pred (clojure.core/fn [%] (clojure.core/contains? % :SOME/K))
   :via [:SOME/SPEC-1 :SOME/SPEC-2]})

(defn- !required-key [problem]
  (when (and (= `s/keys (some-> problem :via peek s/form first))
             (= `contains? (some-> problem :pred last first)))
    (when-some* [k  (some-> problem :pred last last)
                 in (some-> problem :in vec)]
      [(conj in k)])))

(comment
  {:path [:password], :val "1234", :in [:password]
   :pred (clojure.core/fn [%]
           (clojure.core/re-matches #".{8,255}" %)),
   :via []})

(defn- !min-max-regex [problem char-pattern]
  (when (= `re-matches (some-> problem :pred last first))
    (when-some* [pattern (re-pattern (str "\\A" char-pattern "\\{(\\d+),(\\d+)\\}\\z"))
                 regex   (some-> problem :pred last second str)
                 matches (re-matches pattern regex)
                 r-min   (some-> matches (get 1) bigint)
                 r-max   (some-> matches (get 2) bigint)
                 in      (some-> problem :in vec)]
      [in r-min r-max])))

(defn- present-problem [problem]
  (or
   (when-let [[in] (!required-key problem)]
     [in "Обязательное"])
   (when-let [[in r-min r-max] (!min-max-regex problem #"\.")]
     [in (str "Кол-во символов от " r-min " до " r-max)])
   (when-let [[in r-min r-max] (!min-max-regex problem #"\\w")]
     [in (str "Кол-во латинских букв и цифр от " r-min " до " r-max)])
   (let [in (:in problem)]
     [in "Неопознанная ошибка, обратитесь к администратору"])))

(defn present-explain-data [explain-data]
  (let [problems  (::s/problems explain-data)
        pairs (map present-problem  problems)]
    (reduce
     (fn [acc [in message]]
       (errors/add-message acc in message))
     (errors/blank)
     pairs)))
