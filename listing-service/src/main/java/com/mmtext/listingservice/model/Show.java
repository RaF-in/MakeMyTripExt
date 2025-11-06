package com.mmtext.listingservice.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
public class Show {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String title;
    private String theaterName;
    private String screen;
    private OffsetDateTime showTime;
    private int ticketPrice;
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Movie movie;
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Theater theater;

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

    public String getTheaterName() {
        return theaterName;
    }

    public void setTheaterName(String theaterName) {
        this.theaterName = theaterName;
    }

    public String getScreen() {
        return screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public OffsetDateTime getShowTime() {
        return showTime;
    }

    public void setShowTime(OffsetDateTime showTime) {
        this.showTime = showTime;
    }

    public int getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(int ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public Theater getTheater() {
        return theater;
    }

    public void setTheater(Theater theater) {
        this.theater = theater;
    }
}
