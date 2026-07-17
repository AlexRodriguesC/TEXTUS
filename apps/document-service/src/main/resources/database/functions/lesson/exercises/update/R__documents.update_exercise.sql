CREATE OR REPLACE FUNCTION documents.update_exercise(
    p_exercise_id BIGINT,
    p_topic_id BIGINT,
    p_family TEXT,
    p_index INT,
    p_explanation TEXT
) RETURNS VOID AS $$
BEGIN
    UPDATE documents.exercise
    SET topic_id = p_topic_id,
        family = p_family,
        index = p_index,
        explanation = p_explanation
    WHERE id = p_exercise_id;
END;
$$ LANGUAGE plpgsql;