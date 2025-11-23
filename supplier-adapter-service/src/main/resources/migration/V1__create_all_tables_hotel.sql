-- Table for AddressPolled
CREATE TABLE IF NOT EXISTS address_polled (
                                              id BIGSERIAL PRIMARY KEY,  -- Changed from AUTO_INCREMENT
                                              street VARCHAR(255),
    city VARCHAR(255),
    state VARCHAR(255),
    zip VARCHAR(50),
    country VARCHAR(255),
    phone VARCHAR(50),
    email VARCHAR(255),
    fax VARCHAR(50),
    latitude DOUBLE PRECISION,  -- Changed from DOUBLE
    longitude DOUBLE PRECISION,
    geohash VARCHAR(255)
    );

-- Table for HotelPolled
CREATE TABLE IF NOT EXISTS hotel_polled (
                                            id BIGSERIAL PRIMARY KEY,
                                            name VARCHAR(255),
    description TEXT,
    addresses_id BIGINT,
    rating DOUBLE PRECISION,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    ref VARCHAR(255),
    supplier_ref VARCHAR(255),
    FOREIGN KEY (addresses_id) REFERENCES address_polled(id)
    );

-- Table for hotel amenities (ElementCollection)
CREATE TABLE IF NOT EXISTS hotel_polled_amenities (
                                                      hotel_polled_id BIGINT,
                                                      amenities VARCHAR(255),
    FOREIGN KEY (hotel_polled_id) REFERENCES hotel_polled(id)
    );

-- Table for RoomTypePolled
CREATE TABLE IF NOT EXISTS room_type_polled (
                                                id BIGSERIAL PRIMARY KEY,
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
CREATE TABLE IF NOT EXISTS room_type_polled_facilities (
                                                           room_type_polled_id BIGINT,
                                                           facilities VARCHAR(255),
    FOREIGN KEY (room_type_polled_id) REFERENCES room_type_polled(id)
    );

-- Outbox events table
CREATE TABLE IF NOT EXISTS hotel_outbox_events (
                                                   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );