CREATE OR REPLACE FUNCTION documents.create_exercise(
    p_lesson_id BIGINT,
    p_topic_id  BIGINT,
    p_family    TEXT,
    p_index     INTEGER
) RETURNS BIGINT
    LANGUAGE plpgsql
AS $$
DECLARE
    v_id BIGINT;
BEGIN
    INSERT INTO documents.exercise (lesson_id, topic_id, family, index)
    VALUES (p_lesson_id, p_topic_id, p_family, p_index)
    RETURNING id INTO v_id;

    RETURN v_id;
END;
$$;
