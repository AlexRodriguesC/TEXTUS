CREATE OR REPLACE FUNCTION books.read_book(p_id BIGINT)
    RETURNS SETOF books.book
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        SELECT *
        FROM books.book b
        WHERE b.id = p_id;
END;
$$;