CREATE OR REPLACE FUNCTION books.read_index_entries(p_index_id BIGINT)
    RETURNS SETOF books.index_entry
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        SELECT *
        FROM books.index_entry ie
        WHERE ie.index_id = p_index_id;
END;
$$;
