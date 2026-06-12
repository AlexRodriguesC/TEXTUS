CREATE OR REPLACE FUNCTION books.create_book(p_title TEXT)
    RETURNS BIGINT
    LANGUAGE plpgsql AS
$$
DECLARE
    v_id BIGINT;
BEGIN
    INSERT INTO books.book(title) VALUES (p_title) RETURNING id INTO v_id;
    RETURN v_id;
END;
$$;
