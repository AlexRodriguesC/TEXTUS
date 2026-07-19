CREATE OR REPLACE FUNCTION documents.delete_exercises_by_lesson(p_lesson_id bigint)
    RETURNS void
    LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM documents.attempt
    WHERE exercise_id IN (
        SELECT id FROM documents.exercise WHERE lesson_id = p_lesson_id
    );

    DELETE FROM documents.question
    WHERE exercise_id IN (
        SELECT id FROM documents.exercise WHERE lesson_id = p_lesson_id
    );

    DELETE FROM documents.list_exercise
    WHERE exercise_id IN (
        SELECT id FROM documents.exercise WHERE lesson_id = p_lesson_id
    );

    DELETE FROM documents.group_exercise
    WHERE exercise_id IN (
        SELECT id FROM documents.exercise WHERE lesson_id = p_lesson_id
    );

    DELETE FROM documents.pair_exercise
    WHERE exercise_id IN (
        SELECT id FROM documents.exercise WHERE lesson_id = p_lesson_id
    );

    DELETE FROM documents.exercise
    WHERE lesson_id = p_lesson_id;
END;
$$;