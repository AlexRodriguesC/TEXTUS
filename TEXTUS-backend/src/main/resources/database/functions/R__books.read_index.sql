CREATE OR REPLACE FUNCTION books.read_index(p_book_id BIGINT)
    RETURNS SETOF books.index
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        SELECT *
        FROM books.index i
        WHERE i.book_id = p_book_id;
END;
$$;
