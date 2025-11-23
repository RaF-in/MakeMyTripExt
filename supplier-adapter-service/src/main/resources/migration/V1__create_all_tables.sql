-- Table for AddressPolled
CREATE TABLE address_polled (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                street VARCHAR(255),
                                city VARCHAR(255),
                                state VARCHAR(255),
                                zip VARCHAR(50),
                                country VARCHAR(255),
                                phone VARCHAR(50),
                                email VARCHAR(255),
                                fax VARCHAR(50),
                                latitude DOUBLE,
                                longitude DOUBLE,
                                geohash VARCHAR(255)
);

-- Table for HotelPolled
CREATE TABLE hotel_polled (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              name VARCHAR(255),
                              description TEXT,
                              addresses_id BIGINT,
                              rating DOUBLE,
                              created_at TIMESTAMP,
                              updated_at TIMESTAMP,
                              ref VARCHAR(255),
                              supplier_ref VARCHAR(255),
                              FOREIGN KEY (addresses_id) REFERENCES address_polled(id)
);

-- Table for hotel amenities (ElementCollection)
CREATE TABLE hotel_polled_amenities (
                                        hotel_polled_id BIGINT,
                                        amenities VARCHAR(255),
                                        FOREIGN KEY (hotel_polled_id) REFERENCES hotel_polled(id)
);

-- Table for RoomTypePolled
CREATE TABLE room_type_polled (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                  room_type VARCHAR(255),
                                  total_rooms INT,
                                  price_per_night DECIMAL(19, 2),
                                  hotel_id BIGINT,
                                  ref VARCHAR(255),
                                  created_at TIMESTAMP,
                                  updated_at TIMESTAMP,
                                  FOREIGN KEY (hotel_id) REFERENCES hotel_polled(id)
);

-- Table for room facilities (ElementCollection)
CREATE TABLE room_type_polled_facilities (
                                             room_type_polled_id BIGINT,
                                             facilities VARCHAR(255),
                                             FOREIGN KEY (room_type_polled_id) REFERENCES room_type_polled(id)
);
-- Outbox events table - the key to reliable event publishing
CREATE TABLE hotel_outbox_events (
                               id UUID PRIMARY KEY,
                               aggregate_type VARCHAR(255) NOT NULL,
                               type VARCHAR(255) NOT NULL,
                               payload JSONB NOT NULL,
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);