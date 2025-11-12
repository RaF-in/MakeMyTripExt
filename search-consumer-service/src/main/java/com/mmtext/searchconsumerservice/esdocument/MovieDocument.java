package com.mmtext.searchconsumerservice.esdocument;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;


@Document(indexName = "movie")
public class MovieDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

//    @Field(type = FieldType.Keyword)
//    private String language;
//
//    @Field(type = FieldType.Keyword)
//    private String genre;
//
//    @Field(type = FieldType.Integer)
//    private int durationMin;
//
//    @Field(type = FieldType.Keyword)
//    private String rating;
//
//    // IDs of related shows
//    @Field(type = FieldType.Nested, includeInParent = true)
//    private List<ShowDocument> shows;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

