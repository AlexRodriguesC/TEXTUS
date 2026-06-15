CREATE OR REPLACE FUNCTION books.create_summary(p_book_id BIGINT, p_index_id BIGINT, p_content TEXT)
    RETURNS BIGINT
    LANGUAGE plpgsql AS
$$
DECLARE
    v_id BIGINT;
BEGIN
    INSERT INTO books.index_entry_summary(book_id, index_id, content) VALUES (p_book_id, p_index_id, p_content) RETURNING id INTO v_id;
    RETURN v_id;
END;
$$;
