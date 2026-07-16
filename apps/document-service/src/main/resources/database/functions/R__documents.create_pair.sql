CREATE OR REPLACE FUNCTION documents.create_pair(
    p_exercise_id  BIGINT,
    p_request      TEXT,
    p_pairs        JSONB   -- [{"left": "a", "right": "b", "explanation": "..."}, ...]
) RETURNS BIGINT
    LANGUAGE plpgsql
AS $$
DECLARE
    v_pair_exercise_id BIGINT;
    v_pair             JSONB;
BEGIN
    INSERT INTO documents.pair_exercise (exercise_id, request)
    VALUES (p_exercise_id, p_request)
    RETURNING id INTO v_pair_exercise_id;

    FOR v_pair IN SELECT * FROM jsonb_array_elements(p_pairs)
        LOOP
            INSERT INTO documents.pair_answer (pair_exercise_id, left_value, right_value, explanation)
            VALUES (
                       v_pair_exercise_id,
                       v_pair ->> 'left',
                       v_pair ->> 'right',
                       v_pair ->> 'explanation'
                   );
        END LOOP;

    RETURN v_pair_exercise_id;
END;
$$;
