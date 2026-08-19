CREATE OR REPLACE FUNCTION documents.update_path_settings(
    p_space_id BIGINT,
    p_question_percentage INTEGER,
    p_list_percentage INTEGER,
    p_group_percentage INTEGER,
    p_pair_percentage INTEGER
)
    RETURNS VOID
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE documents.learning_path_settings s
    SET question_percentage = p_question_percentage,
        list_percentage     = p_list_percentage,
        group_percentage    = p_group_percentage,
        pair_percentage     = p_pair_percentage
    FROM documents.learning_path lp
    WHERE lp.space_id = p_space_id
      AND s.path_id = lp.id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'No learning path settings found for space id %', p_space_id;
    END IF;
END;
$$;