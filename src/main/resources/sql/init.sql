CREATE TABLE IF NOT EXISTS backpacks (
    id CHAR(36) NOT NULL,
    page INT NOT NULL,
    data LONGBLOB NOT NULL,
    PRIMARY KEY (id, page)
);