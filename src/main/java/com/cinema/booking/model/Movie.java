package com.cinema.booking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tytuł jest wymagany")
    private String title;

    @Column(length = 2000)
    private String description;

    @NotBlank(message = "Gatunek jest wymagany")
    private String genre;

    @NotBlank(message = "Reżyser jest wymagany")
    private String director;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "movie_actors",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "actor_id")
    )
    @Builder.Default
    private List<Actor> actors = new ArrayList<>();

    @Min(value = 0, message = "Czas trwania musi być dodatni")
    private Integer durationMinutes;

    private Integer ageRestriction;

    private String posterUrl;
    private String trailerUrl;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MovieImage> images = new ArrayList<>();
}