package com.goldencinema.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "movies")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false)
    private String genre;

    @Column(nullable = false)
    private String ageRating;

    public Movie() {
    }

    public Movie(String title, String description, Integer durationMinutes, String genre, String ageRating) {
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.genre = genre;
        this.ageRating = ageRating;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public String getGenre() {
        return genre;
    }

    public String getAgeRating() {
        return ageRating;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }
}