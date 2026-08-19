CREATE OR REPLACE FUNCTION documents.update_pair(
    p_exercise_id  BIGINT,
    p_request      TEXT,
    p_pairs        JSONB
) RETURNS BIGINT
    LANGUAGE plpgsql
AS $$
DECLARE
    v_pair_exercise_id BIGINT;
    v_pair             JSONB;
BEGIN
    UPDATE documents.pair_exercise
    SET request = p_request
    WHERE exercise_id = p_exercise_id
    RETURNING id INTO v_pair_exercise_id;

    DELETE FROM documents.pair_answer
    WHERE pair_exercise_id = v_pair_exercise_id;

    FOR v_pair IN SELECT * FROM jsonb_array_elements(p_pairs)
        LOOP
            INSERT INTO documents.pair_answer (pair_exercise_id, left_value, right_value)
            VALUES (
                       v_pair_exercise_id,
                       v_pair ->> 'left',
                       v_pair ->> 'right'
                   );
        END LOOP;

    RETURN v_pair_exercise_id;
END;
$$;