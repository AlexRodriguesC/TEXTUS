DROP FUNCTION IF EXISTS documents.create_chapter(BIGINT, BIGINT, INTEGER, TEXT, TEXT);
DROP FUNCTION IF EXISTS documents.read_chapters(BIGINT);

ALTER TABLE documents.chapter
    ADD COLUMN document_id BIGINT NOT NULL
        REFERENCES documents.document(id);

CREATE INDEX chapter_path_id_document_id_idx
    ON documents.chapter (path_id, document_id);
