CREATE TABLE IF NOT EXISTS delivery_note
(
    id             SERIAL PRIMARY KEY,
    created_at     TIMESTAMP WITH TIME ZONE,
    updated_at     TIMESTAMP WITH TIME ZONE,
    issuer_code    TEXT,
    deliverer_code TEXT,
    receiver_code  TEXT,
    uuid           TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS address
(
    id                SERIAL PRIMARY KEY,
    created_at        TIMESTAMP WITH TIME ZONE,
    updated_at        TIMESTAMP WITH TIME ZONE,
    is_active         BOOLEAN NOT NULL DEFAULT TRUE,
    address_type      TEXT,
    delivery_note_id  BIGINT REFERENCES delivery_note (id),
    full_address      TEXT,
    address_id        TEXT,
    ehak_county       TEXT,
    county            TEXT,
    ehak_municipality TEXT,
    municipality      TEXT,
    ehak_settlement   TEXT,
    settlement        TEXT,
    address_text      TEXT,
    address_number    TEXT,
    code_address      TEXT,
    coordinate_x      TEXT,
    coordinate_y      TEXT,
    ads_oid           TEXT,
    adob_id           TEXT
);

CREATE OR REPLACE FUNCTION update_validity()
    RETURNS TRIGGER AS
$$
BEGIN
    UPDATE address
    SET is_active = FALSE
    WHERE delivery_note_id = NEW.delivery_note_id
      AND address_type = NEW.address_type;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER before_insert_address
    BEFORE INSERT
    ON address
    FOR EACH ROW
EXECUTE FUNCTION update_validity();
