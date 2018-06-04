-- :name- truncate-all :!
DO $$
DECLARE
  statements CURSOR FOR
  SELECT tablename FROM pg_tables
  WHERE schemaname = 'public'
    AND tablename != 'flyway_schema_history';
BEGIN
    FOR stmt IN statements LOOP
        EXECUTE 'TRUNCATE TABLE ' || quote_ident(stmt.tablename);
    END LOOP;
END $$
