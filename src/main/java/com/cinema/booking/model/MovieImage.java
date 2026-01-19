package com.cinema.booking.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "movie_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private Movie movie;
}