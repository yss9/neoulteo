ALTER TABLE travel_plan_places
    ADD COLUMN day_no INT NOT NULL DEFAULT 1 AFTER plan_id;
