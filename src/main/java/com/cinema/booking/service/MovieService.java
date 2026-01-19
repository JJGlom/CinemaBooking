package com.cinema.booking.service;

import com.cinema.booking.exception.ResourceNotFoundException;
import com.cinema.booking.model.Movie;
import com.cinema.booking.model.MovieImage;
import com.cinema.booking.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final String UPLOAD_DIR = "uploads/";

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public Page<Movie> getAllMovies(Pageable pageable) {
        return movieRepository.findAll(pageable);
    }

    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono filmu o id: " + id));
    }

    @Transactional
    public Movie addMovie(Movie movie, List<MultipartFile> galleryFiles) {
        processGalleryFiles(movie, galleryFiles);
        return movieRepository.save(movie);
    }

    @Transactional
    public Movie updateMovie(Long id, Movie movieDetails, List<MultipartFile> galleryFiles) {
        Movie movie = getMovieById(id);
        movie.setTitle(movieDetails.getTitle());
        movie.setDescription(movieDetails.getDescription());
        movie.setGenre(movieDetails.getGenre());
        movie.setDirector(movieDetails.getDirector());
        movie.setDurationMinutes(movieDetails.getDurationMinutes());
        movie.setAgeRestriction(movieDetails.getAgeRestriction());
        movie.setCastMembers(movieDetails.getCastMembers());
        movie.setTrailerUrl(movieDetails.getTrailerUrl());

        if (movieDetails.getPosterUrl() != null) {
            movie.setPosterUrl(movieDetails.getPosterUrl());
        }

        processGalleryFiles(movie, galleryFiles);

        return movieRepository.save(movie);
    }

    @Transactional
    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
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
            throw new RuntimeException("Nie udało się zapisać pliku: " + e.getMessage());
        }
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