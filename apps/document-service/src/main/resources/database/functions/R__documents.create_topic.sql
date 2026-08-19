CREATE OR REPLACE FUNCTION documents.create_topic(
    p_page_id BIGINT,
    p_title   TEXT,
    p_summary TEXT,
    p_labels  TEXT[],
    p_types   TEXT[]
)
    RETURNS BIGINT
    LANGUAGE plpgsql
AS $$
DECLARE
    v_topic_id BIGINT;
    v_label_id BIGINT;
    v_label    TEXT;
    v_type     TEXT;
    v_types    TEXT[];
BEGIN
    INSERT INTO documents.topic (page_id, title, summary)
    VALUES (p_page_id, p_title, p_summary)
    RETURNING id INTO v_topic_id;

    FOREACH v_label IN ARRAY COALESCE(p_labels, '{}'::TEXT[])
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

    IF p_types IS NULL OR cardinality(p_types) = 0 THEN
        v_types := ARRAY['QUESTION']::TEXT[];
    ELSE
        v_types := p_types;
    END IF;

    FOREACH v_type IN ARRAY v_types
        LOOP
            INSERT INTO documents.topic_type (topic_id, type)
            VALUES (v_topic_id, v_type)
            ON CONFLICT (topic_id, type) DO NOTHING;
        END LOOP;

    RETURN v_topic_id;
END;
$$;
