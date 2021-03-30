(ns publicator.utils.datascript
  "Мне нужно мержить DB, например чтобы объединять валидаторы.
  ")

;; а может быть тут и filtered-db сойдет.
;; ну или потом законтрибьютить filter-materialize

;; filtered db is read-only
;; наверное отсюда нужно вытащить db, а то больно сложная логика у фукнции
(defn filter-datoms [agg allowed-attr?]
  (let [pred   (comp (some-fn #(= "db" (namespace %))
                              allowed-attr?)
                     :a)
        datoms (->> (d/datoms agg :eavt)
                    (filter pred))
        schema (d/schema agg)]
    (d/init-db datoms schema)))
