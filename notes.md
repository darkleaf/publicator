# Как заполнить дефолтные скрытые поля у вложенных сущностей?
Берешь tx-data, которая в виде списка датомов, если там есть что-то вроде
`[10 :publication.translation/publication 1]`, то это значит, что добавилась вложенная сущность.
Нужна функция `tx-data -> tx-data` и в ней нужно делать `mapcat`.

# "conflict free" не работает
Не нужно надеяться, что транзакции накатятся на следующую версию
По ходу нужно `data/diff` использовать.

# Сделать subject, в котором поля автора и пользователя
наче больно сложно получается
и делать не роли, а флаги

# хранить вложенные модели в транспонированном виде
`"publication.translation/title" string[]`
для полнотекста исползьовать string[] -> tsvector и автовычисляемые колонки
массив дат можно так-же преобразовать в диапозон дат и проиндексировать

```
create or replace function intarray2int4range(arr int[]) returns int4range as $$
  select int4range(min(val), max(val) + 1) from unnest(arr) as val;
$$ language sql immutable;
```


# React.createElement
можно попробовать написать копих этой функции и посмотреть, вдруг closure compiler ее заинлайнет


# store keywords
лучше не хранить значения в виде keyword, а использовать строки, проблемы конвертацией в jdbc


# фильтрация внутри
```sql
select *
from "user"
join lateral unnest("e:author.translation/last-name") with ordinality as e(e, idx) on true
join lateral unnest("v:author.translation/last-name") with ordinality as v(v, idx)
  on e.idx = v.idx;
```
