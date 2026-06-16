CREATE OR REPLACE FUNCTION books.create_index(p_book_id BIGINT, p_page INT)
    RETURNS BIGINT
    LANGUAGE plpgsql AS
$$
DECLARE
    v_id BIGINT;
BEGIN
    INSERT INTO books.index(book_id, page) VALUES (p_book_id, p_page) RETURNING id INTO v_id;
    RETURN v_id;
END;
$$;
