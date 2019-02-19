ссылка должна проверять, что не изменился тип и идентификатор корня

значение (состояние) должно проверять свою целостность



```
(let [counter (volatile! 0)]
  (->> #(future
          (locking counter
            (vswap! counter inc)))
       (repeatedly)
       (take 10)
       (doall)
       (map deref)
       (doall))
  @counter)
```
