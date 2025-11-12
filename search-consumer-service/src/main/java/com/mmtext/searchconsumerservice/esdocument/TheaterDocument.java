package com.mmtext.searchconsumerservice.esdocument;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.List;

@Document(indexName = "theaters")
public class TheaterDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Nested, includeInParent = true)
    private AddressDocument address;

//    @Field(type = FieldType.Nested, includeInParent = true)
//    private List<ShowDocument> shows;

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
}
