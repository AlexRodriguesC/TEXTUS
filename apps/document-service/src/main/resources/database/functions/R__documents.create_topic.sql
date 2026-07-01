CREATE OR REPLACE FUNCTION documents.create_topic(
    p_page_id     BIGINT,
    p_tokens_in   INTEGER,
    p_tokens_out  INTEGER,
    p_title       TEXT,
    p_summary     TEXT,
    p_labels      TEXT[]
)
    RETURNS BIGINT
    LANGUAGE plpgsql
AS $$
DECLARE
    v_topic_id BIGINT;
    v_label_id BIGINT;
    v_label    TEXT;
BEGIN
    INSERT INTO documents.topic (page_id, tokens_in, tokens_out, title, summary)
    VALUES (p_page_id, p_tokens_in, p_tokens_out, p_title, p_summary)
    RETURNING id INTO v_topic_id;

    FOREACH v_label IN ARRAY p_labels
        LOOP
            SELECT id INTO v_label_id
            FROM documents.label
            WHERE label = v_label;

            IF v_label_id IS NULL THEN
                INSERT INTO documents.label (label)
                VALUES (v_label)
                RETURNING id INTO v_label_id;
            END IF;

            INSERT INTO documents.topic_label (topic_id, label_id)
            VALUES (v_topic_id, v_label_id)
            ON CONFLICT (topic_id, label_id) DO NOTHING;
        END LOOP;

    RETURN v_topic_id;
END;
$$;