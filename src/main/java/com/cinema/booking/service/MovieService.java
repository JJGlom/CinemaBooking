package com.cinema.booking.service;

import com.cinema.booking.exception.ResourceNotFoundException;
import com.cinema.booking.model.Actor;
import com.cinema.booking.model.Movie;
import com.cinema.booking.model.MovieImage;
import com.cinema.booking.repository.ActorRepository;
import com.cinema.booking.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final ActorRepository actorRepository;
    private final String UPLOAD_DIR = "uploads/";

    public List<Movie> getAllMovies() {
        log.info("Pobieranie listy wszystkich filmów");
        return movieRepository.findAll();
    }

    public Page<Movie> getAllMovies(Pageable pageable) {
        log.info("Pobieranie stronicowanej listy filmów: strona {}", pageable.getPageNumber());
        return movieRepository.findAll(pageable);
    }

    public Movie getMovieById(Long id) {
        log.debug("Szukanie filmu o id: {}", id);
        return movieRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Nie znaleziono filmu o id: {}", id);
                    return new ResourceNotFoundException("Nie znaleziono filmu o id: " + id);
                });
    }

    @Transactional
    public Movie addMovie(Movie movie, String castMembersString, List<MultipartFile> galleryFiles) {
        log.info("Dodawanie nowego filmu: {}", movie.getTitle());
        processActors(movie, castMembersString);
        processGalleryFiles(movie, galleryFiles);
        return movieRepository.save(movie);
    }

    @Transactional
    public Movie updateMovie(Long id, Movie movieDetails, String castMembersString, List<MultipartFile> galleryFiles) {
        log.info("Aktualizacja filmu o id: {}", id);
        Movie movie = getMovieById(id);

        movie.setTitle(movieDetails.getTitle());
        movie.setDescription(movieDetails.getDescription());
        movie.setGenre(movieDetails.getGenre());
        movie.setDirector(movieDetails.getDirector());
        movie.setDurationMinutes(movieDetails.getDurationMinutes());
        movie.setAgeRestriction(movieDetails.getAgeRestriction());
        movie.setTrailerUrl(movieDetails.getTrailerUrl());

        if (movieDetails.getPosterUrl() != null) {
            movie.setPosterUrl(movieDetails.getPosterUrl());
        }

        processActors(movie, castMembersString);
        processGalleryFiles(movie, galleryFiles);

        return movieRepository.save(movie);
    }

    @Transactional
    public void deleteMovie(Long id) {
        log.warn("Usuwanie filmu o id: {}", id);
        if (!movieRepository.existsById(id)) {
            log.error("Próba usunięcia nieistniejącego filmu: {}", id);
            throw new ResourceNotFoundException("Nie można usunąć. Film o id " + id + " nie istnieje");
        }
        movieRepository.deleteById(id);
    }

    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) return null;
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            }
            return "/uploads/" + fileName;
        } catch (IOException e) {
            log.error("Błąd zapisu pliku: {}", e.getMessage());
            throw new RuntimeException("Nie udało się zapisać pliku: " + e.getMessage());
        }
    }

    private void processActors(Movie movie, String castMembersString) {
        if (castMembersString == null || castMembersString.isBlank()) {
            movie.setActors(new ArrayList<>());
            return;
        }

        List<Actor> actors = Arrays.stream(castMembersString.split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .map(name -> actorRepository.findByName(name)
                        .orElseGet(() -> actorRepository.save(Actor.builder().name(name).build())))
                .collect(Collectors.toList());

        movie.setActors(actors);
    }

    private void processGalleryFiles(Movie movie, List<MultipartFile> galleryFiles) {
        if (galleryFiles != null && !galleryFiles.isEmpty()) {
            for (MultipartFile file : galleryFiles) {
                if (!file.isEmpty()) {
                    String path = storeFile(file);
                    MovieImage image = MovieImage.builder()
                            .imageUrl(path)
                            .movie(movie)
                            .build();
                    movie.getImages().add(image);
                }
            }
        }
    }
}