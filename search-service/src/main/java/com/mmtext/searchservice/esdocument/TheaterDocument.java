package com.mmtext.searchservice.esdocument;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Document(indexName = "theaters")
public class TheaterDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Nested, includeInParent = true)
    private AddressDocument address;

    @Field(type = FieldType.Nested, includeInParent = true)
    private List<ShowDocument> shows;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AddressDocument getAddress() {
        return address;
    }

    public void setAddress(AddressDocument address) {
        this.address = address;
    }

    public List<ShowDocument> getShows() {
        return shows;
    }

    public void setShows(List<ShowDocument> shows) {
        this.shows = shows;
    }
}
