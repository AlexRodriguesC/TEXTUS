CREATE OR REPLACE FUNCTION documents.read_pair(p_exercise_id BIGINT)
    RETURNS TABLE(
                     id BIGINT,
                     request TEXT,
                     pairs JSONB
                 )
    LANGUAGE plpgsql
AS $$
DECLARE
    v_pair_exercise_id BIGINT;
    v_pairs_array JSONB;
BEGIN
    SELECT pe.id
    INTO v_pair_exercise_id
    FROM documents.pair_exercise pe
    WHERE pe.exercise_id = p_exercise_id;

    SELECT jsonb_agg(
                   jsonb_build_object(
                           'left', pa.left_value,
                           'right', pa.right_value
                   )
                   ORDER BY pa.id
           )
    INTO v_pairs_array
    FROM documents.pair_answer pa
    WHERE pa.pair_exercise_id = v_pair_exercise_id;

    IF v_pairs_array IS NULL THEN
        v_pairs_array := '[]'::JSONB;
    END IF;

    RETURN QUERY
        SELECT
            pe.id,
            pe.request,
            v_pairs_array
        FROM documents.pair_exercise pe
        WHERE pe.exercise_id = p_exercise_id;
END;
$$;
