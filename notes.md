# Как заполнить дефолтные скрытые поля у вложенных сущностей?
Берешь tx-data, которая в виде списка датомов, если там есть что-то вроде
`[10 :publication.translation/publication 1]`, то это значит, что добавилась вложенная сущность.
Нужна функция `tx-data -> tx-data` и в ней нужно делать `mapcat`.

# "conflict free" не работает
Не нужно надеяться, что транзакции накатятся на следующую версию
По ходу нужно `data/diff` использовать.

# Сделать subject, в котором поля автора и пользователя
иначе больно сложно получается
и делать не роли, а флаги


# React.createElement
можно попробовать написать копих этой функции и посмотреть, вдруг closure compiler ее заинлайнет

# Persistence


https://info.crunchydata.com/blog/how-your-postgresql-data-model-affects-storage

## хранить вложенные модели в транспонированном виде
`"publication.translation/title" string[]`
для полнотекста исползьовать string[] -> tsvector и автовычисляемые колонки
массив чисел, дат не не получится индексировать, т.к. нужна операция вроде
`{1,5,7,10} @@ (-inf, 1] = true`, `{1,5,7,10} @@ (7, 10) = false`.
может быть и не так сложно сделать такой оператор `array && range` и поддержку GIN.

Смысла от подобного преобразования мало:
```
create or replace function intarray2int4range(arr int[]) returns int4range as $$
  select int4range(min(val), max(val) + 1) from unnest(arr) as val;
$$ language sql immutable;
```

## два поля - фигня

нужен кастомный тип вроде

```
create type int_ev as (
  e int[],
  v int[]
);
```

индексируется он так `CREATE INDEX ON t USING gin( ((f).v) );`

## фильтрация внутри


```sql
select *
from "user"
join lateral unnest("e:author.translation/last-name") with ordinality as e(e, idx) on true
join lateral unnest("v:author.translation/last-name") with ordinality as v(v, idx)
  on e.idx = v.idx;
```

оно вроде медленно работает
