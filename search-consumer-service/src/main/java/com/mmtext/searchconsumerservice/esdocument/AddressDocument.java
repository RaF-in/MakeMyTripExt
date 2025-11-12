package com.mmtext.searchconsumerservice.esdocument;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;


public class AddressDocument {

    @Field(type = FieldType.Text)
    private String street;

    @Field(type = FieldType.Text)
    private String city;

    @Field(type = FieldType.Text)
    private String state;

    @Field(type = FieldType.Keyword)
    private String zip;

    @Field(type = FieldType.Keyword)
    private String country;

//    @GeoPointField
//    private GeoPoint location;

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

//    public GeoPoint getLocation() {
//        return location;
//    }
//
//    public void setLocation(GeoPoint location) {
//        this.location = location;
//    }
}
