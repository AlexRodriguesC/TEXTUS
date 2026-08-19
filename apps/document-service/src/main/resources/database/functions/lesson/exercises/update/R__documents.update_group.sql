CREATE OR REPLACE FUNCTION documents.update_group(
    p_exercise_id  BIGINT,
    p_request      TEXT,
    p_answer       JSONB
) RETURNS BIGINT
    LANGUAGE plpgsql
AS $$
DECLARE
    v_group_exercise_id BIGINT;
    v_group_answer_id   BIGINT;
    v_label             TEXT;
    v_items             JSONB;
    v_item              TEXT;
BEGIN
    UPDATE documents.group_exercise
    SET request = p_request
    WHERE exercise_id = p_exercise_id
    RETURNING id INTO v_group_exercise_id;

    DELETE FROM documents.group_answer_item
    WHERE group_answer_id IN (
        SELECT id FROM documents.group_answer
        WHERE group_exercise_id = v_group_exercise_id
    );

    DELETE FROM documents.group_answer
    WHERE group_exercise_id = v_group_exercise_id;

    FOR v_label, v_items IN SELECT * FROM jsonb_each(p_answer)
        LOOP
            INSERT INTO documents.group_answer (group_exercise_id, label)
            VALUES (v_group_exercise_id, v_label)
            RETURNING id INTO v_group_answer_id;

            FOR v_item IN SELECT jsonb_array_elements_text(v_items)
                LOOP
                    INSERT INTO documents.group_answer_item (group_answer_id, item)
                    VALUES (v_group_answer_id, v_item);
                END LOOP;
        END LOOP;

    RETURN v_group_exercise_id;
END;
$$;