CREATE OR REPLACE FUNCTION books.create_index(p_book_id BIGINT, p_title TEXT, p_position INT, p_from_page INT, p_to_page INT)
    RETURNS BIGINT
    LANGUAGE plpgsql AS
$$
DECLARE
    v_id BIGINT;
BEGIN
    INSERT INTO books.index(book_id, title, position, from_page, to_page) VALUES (p_book_id, p_title, p_position, p_from_page, p_to_page) RETURNING id INTO v_id;
    RETURN v_id;
END;
$$;
