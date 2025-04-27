CREATE TABLE complaints (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    created_by UUID NOT NULL,
    content TEXT NOT NULL,
    country VARCHAR(2) NOT NULL,
    complaint_counter INT NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    CONSTRAINT unique_user_product UNIQUE (created_by, product_id)
);