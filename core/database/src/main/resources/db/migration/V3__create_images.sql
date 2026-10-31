CREATE TABLE images (
    id                UUID         NOT NULL PRIMARY KEY,
    original_filename VARCHAR(255) NOT NULL,
    format            VARCHAR(16)  NOT NULL,
    size_bytes        BIGINT       NOT NULL,
    uploaded_at       TIMESTAMP    NOT NULL
);

CREATE INDEX idx_images_uploaded_at ON images (uploaded_at);
