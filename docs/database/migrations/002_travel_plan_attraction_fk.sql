ALTER TABLE travel_plan_places
    ADD COLUMN attraction_content_id INT NULL AFTER place_name;

ALTER TABLE travel_plan_places
    ADD CONSTRAINT fk_travel_plan_places_attraction
        FOREIGN KEY (attraction_content_id)
        REFERENCES attractions (content_id)
        ON DELETE RESTRICT;
