CREATE UNIQUE INDEX exercise_one_in_progress_per_lesson
    ON documents.exercise (lesson_id)
    WHERE completed = false;
