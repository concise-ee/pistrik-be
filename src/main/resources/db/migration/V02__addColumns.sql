ALTER TABLE delivery_note
    ADD COLUMN IF NOT EXISTS issuer_name    TEXT,
    ADD COLUMN IF NOT EXISTS deliverer_name TEXT,
    ADD COLUMN IF NOT EXISTS receiver_name  TEXT;