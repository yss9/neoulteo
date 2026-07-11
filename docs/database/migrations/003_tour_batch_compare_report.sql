ALTER TABLE tour_batch_reports
    ADD COLUMN new_item_count INT NOT NULL DEFAULT 0 AFTER item_count,
    ADD COLUMN changed_item_count INT NOT NULL DEFAULT 0 AFTER new_item_count,
    ADD COLUMN missing_item_count INT NOT NULL DEFAULT 0 AFTER changed_item_count,
    ADD COLUMN unchanged_item_count INT NOT NULL DEFAULT 0 AFTER missing_item_count,
    ADD COLUMN change_summary TEXT AFTER unchanged_item_count;
