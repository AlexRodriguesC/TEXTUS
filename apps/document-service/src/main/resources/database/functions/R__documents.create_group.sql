CREATE OR REPLACE FUNCTION documents.create_group(
    p_exercise_id  BIGINT,
    p_request      TEXT,
    p_explanation  TEXT,
    p_answer       JSONB   -- {"label1": ["item1","item2"], "label2": [...]}
) RETURNS BIGINT
    LANGUAGE plpgsql
AS $$
DECLARE
    v_group_exercise_id BIGINT;
    v_group_answer_id   BIGINT;
    v_label_id          BIGINT;
    v_label             TEXT;
    v_items             JSONB;
    v_item              TEXT;
BEGIN
    INSERT INTO documents.group_exercise (exercise_id, request)
    VALUES (p_exercise_id, p_request)
    RETURNING id INTO v_group_exercise_id;

    INSERT INTO documents.group_answer (group_exercise_id, explanation)
    VALUES (v_group_exercise_id, p_explanation)
    RETURNING id INTO v_group_answer_id;

    FOR v_label, v_items IN SELECT * FROM jsonb_each(p_answer)
        LOOP
            INSERT INTO documents.group_answer_label (group_answer_id, label)
            VALUES (v_group_answer_id, v_label)
            RETURNING id INTO v_label_id;

            FOR v_item IN SELECT jsonb_array_elements_text(v_items)
                LOOP
                    INSERT INTO documents.group_answer_item (label_id, item)
                    VALUES (v_label_id, v_item);
                END LOOP;
        END LOOP;

    RETURN v_group_exercise_id;
END;
$$;
