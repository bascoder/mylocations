CREATE TABLE location_item(
		id INTEGER PRIMARY KEY,
		longitude REAL,
		latitude REAL,
		altitude REAL,
		place TEXT,
		country TEXT,
		flagPath TEXT
);

CREATE TABLE location_time(
    item_id INTEGER PRIMARY KEY,
    datum_tijdstamp BIGINT DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (item_id) 
        REFERENCES location_item(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE location_picture(
    item_id INTEGER PRIMARY KEY,
    pictureURI TEXT,
    
    FOREIGN KEY (item_id) 
        REFERENCES location_item(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);