package com.mmtext.listingservice.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
public class Concert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private Address venue;
    private OffsetDateTime date;
    private OffsetDateTime time;
    private String organizer;

    @OneToMany(mappedBy = "concert", cascade = CascadeType.ALL)
    private List<TicketCategory> ticketCategories;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Address getVenue() {
        return venue;
    }

    public void setVenue(Address venue) {
        this.venue = venue;
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public void setDate(OffsetDateTime date) {
        this.date = date;
    }

    public OffsetDateTime getTime() {
        return time;
    }

    public void setTime(OffsetDateTime time) {
        this.time = time;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public List<TicketCategory> getTicketCategories() {
        return ticketCategories;
    }

    public void setTicketCategories(List<TicketCategory> ticketCategories) {
        this.ticketCategories = ticketCategories;
    }
}
