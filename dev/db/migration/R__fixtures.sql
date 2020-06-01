truncate table "user";

insert into "user" values
(-1, 'active', true, true, 'admin', 'digest',
 '{2, 3}', '{"en", "ru"}', '{"John", "Иван"}', '{"Doe", "Иванов"}');
