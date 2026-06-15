CREATE OR REPLACE FUNCTION books.delete_book(p_id BIGINT)
    RETURNS bool
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_id BIGINT;
BEGIN
    SELECT * FROM books.book book WHERE book.id = p_id INTO v_id;
    IF v_id IS NULL THEN
        RETURN false;
    END IF;

    DELETE FROM books.book book WHERE book.id = v_id;

    RETURN true;
END;
$$;