package com.mmtext.searchconsumerservice.esdocument;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

/**
 * Elasticsearch document for Address data.
 * Optimized for geo-spatial queries and location-based searches.
 */

@Document(indexName = "addresses")
public class AddressDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String street;

    @Field(type = FieldType.Keyword)
    private String city;

    @Field(type = FieldType.Keyword)
    private String state;

    @Field(type = FieldType.Keyword)
    private String zip;

    @Field(type = FieldType.Keyword)
    private String country;

    @Field(type = FieldType.Keyword)
    private String phone;

    @Field(type = FieldType.Keyword)
    private String email;

    @Field(type = FieldType.Keyword)
    private String fax;

    /**
     * Geo-point field for location-based queries.
     * Supports distance, bounding box, and polygon queries.
     */
    @GeoPointField
    private GeoPoint location;

    /**
     * Geohash for efficient prefix-based geo searches.
     */
    @Field(type = FieldType.Keyword)
    private String geohash;

    /**
     * Convenience method to set location from latitude and longitude.
     */
    public void setCoordinates(Double latitude, Double longitude) {
        if (latitude != null && longitude != null) {
            this.location = new GeoPoint(latitude, longitude);
        }
    }

    /**
     * Get latitude from location.
     */
    public Double getLatitude() {
        return location != null ? location.getLat() : null;
    }

    /**
     * Get longitude from location.
     */
    public Double getLongitude() {
        return location != null ? location.getLon() : null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public String getGeohash() {
        return geohash;
    }

    public void setGeohash(String geohash) {
        this.geohash = geohash;
    }
}