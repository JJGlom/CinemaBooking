package com.cinema.booking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

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

    private String castMembers;

    @Min(value = 0, message = "Czas trwania musi być dodatni")
    private Integer durationMinutes;

    private Integer ageRestriction;

    private String posterUrl;
    private String trailerUrl;
}