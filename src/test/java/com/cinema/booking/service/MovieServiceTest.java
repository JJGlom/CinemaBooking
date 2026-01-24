package com.cinema.booking.service;

import com.cinema.booking.exception.ResourceNotFoundException;
import com.cinema.booking.model.Actor;
import com.cinema.booking.model.Movie;
import com.cinema.booking.model.MovieImage;
import com.cinema.booking.repository.ActorRepository;
import com.cinema.booking.repository.MovieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ActorRepository actorRepository;

    @InjectMocks
    private MovieService movieService;

    @Test
    void shouldGetAllMovies() {
        Movie movie = Movie.builder().title("Test").build();
        when(movieRepository.findAll()).thenReturn(List.of(movie));
        List<Movie> result = movieService.getAllMovies();
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldGetAllMoviesPaged() {
        Movie movie = Movie.builder().title("Test").build();
        Page<Movie> page = new PageImpl<>(List.of(movie));
        Pageable pageable = PageRequest.of(0, 10);
        when(movieRepository.findAll(pageable)).thenReturn(page);
        Page<Movie> result = movieService.getAllMovies(pageable);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldGetMovieById() {
        Movie movie = Movie.builder().id(1L).title("Test").build();
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        Movie result = movieService.getMovieById(1L);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void shouldThrowExceptionWhenMovieNotFound() {
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> movieService.getMovieById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldAddMovieWithActorsAndGallery() {
        Movie movie = Movie.builder().title("New").build();
        String cast = "Actor1, Actor2";
        MockMultipartFile file = new MockMultipartFile("gallery", "test.jpg", "image/jpeg", "data".getBytes());

        when(actorRepository.findByName(any())).thenReturn(Optional.empty());
        when(actorRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(movieRepository.save(movie)).thenReturn(movie);

        movieService.addMovie(movie, cast, List.of(file));

        assertThat(movie.getActors()).hasSize(2);
        assertThat(movie.getImages()).hasSize(1);
    }

    @Test
    void shouldDoNothingWhenGalleryListIsNull() {
        Movie movie = Movie.builder().title("New").build();
        when(movieRepository.save(movie)).thenReturn(movie);

        movieService.addMovie(movie, null, null);

        assertThat(movie.getImages()).isEmpty();
    }

    @Test
    void shouldHandleEmptyGalleryList() {
        Movie movie = Movie.builder().title("New").build();
        when(movieRepository.save(movie)).thenReturn(movie);

        movieService.addMovie(movie, null, Collections.emptyList());

        assertThat(movie.getImages()).isEmpty();
    }

    @Test
    void shouldReuseExistingActor() {
        Movie movie = Movie.builder().title("New").build();
        String cast = "Existing Actor";
        Actor existingActor = Actor.builder().id(1L).name("Existing Actor").build();

        when(actorRepository.findByName("Existing Actor")).thenReturn(Optional.of(existingActor));
        when(movieRepository.save(movie)).thenReturn(movie);

        movieService.addMovie(movie, cast, null);

        assertThat(movie.getActors()).hasSize(1);
        assertThat(movie.getActors().get(0).getId()).isEqualTo(1L);
        verify(actorRepository, never()).save(any());
    }

    @Test
    void shouldUpdateMovieWithNewDetails() {
        Long id = 1L;
        Movie existingMovie = Movie.builder().id(id).title("Old").posterUrl("old.jpg").build();
        Movie movieDetails = Movie.builder()
                .title("New")
                .description("Desc")
                .genre("Action")
                .director("Dir")
                .durationMinutes(100)
                .ageRestriction(12)
                .trailerUrl("url")
                .posterUrl("new.jpg")
                .build();

        when(movieRepository.findById(id)).thenReturn(Optional.of(existingMovie));
        when(movieRepository.save(existingMovie)).thenReturn(existingMovie);

        Movie result = movieService.updateMovie(id, movieDetails, "", null, null);

        assertThat(result.getTitle()).isEqualTo("New");
        assertThat(result.getPosterUrl()).isEqualTo("new.jpg");
        verify(movieRepository).save(existingMovie);
    }

    @Test
    void shouldUpdateMovieWithoutChangingPosterUrlWhenNull() {
        Long id = 1L;
        Movie existingMovie = Movie.builder().id(id).title("Old").posterUrl("keep_me.jpg").build();
        Movie movieDetails = Movie.builder()
                .title("New")
                .description("Desc")
                .genre("Action")
                .director("Dir")
                .durationMinutes(100)
                .ageRestriction(12)
                .trailerUrl("url")
                .posterUrl(null)
                .build();

        when(movieRepository.findById(id)).thenReturn(Optional.of(existingMovie));
        when(movieRepository.save(existingMovie)).thenReturn(existingMovie);

        Movie result = movieService.updateMovie(id, movieDetails, "", null, null);

        assertThat(result.getTitle()).isEqualTo("New");
        assertThat(result.getPosterUrl()).isEqualTo("keep_me.jpg");
        verify(movieRepository).save(existingMovie);
    }

    @Test
    void shouldUpdateMovieWithNullCastAndGallery() {
        Long id = 1L;
        Movie existingMovie = Movie.builder().id(id).title("Old").build();
        Movie movieDetails = Movie.builder().title("New").build();

        when(movieRepository.findById(id)).thenReturn(Optional.of(existingMovie));
        when(movieRepository.save(existingMovie)).thenReturn(existingMovie);

        movieService.updateMovie(id, movieDetails, null, null, null);

        assertThat(existingMovie.getActors()).isEmpty();
    }

    @Test
    void shouldDeleteMovie() {
        Long id = 1L;
        when(movieRepository.existsById(id)).thenReturn(true);
        movieService.deleteMovie(id);
        verify(movieRepository).deleteById(id);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentMovie() {
        Long id = 1L;
        when(movieRepository.existsById(id)).thenReturn(false);
        assertThatThrownBy(() -> movieService.deleteMovie(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldHandleFileStorageException() {
        MockMultipartFile file = spy(new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes()));
        try {
            doThrow(new IOException("Error")).when(file).getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertThatThrownBy(() -> movieService.storeFile(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Nie udało się zapisać pliku");
    }

    @Test
    void shouldHandleFileWithoutExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "testfile", "image/jpeg", "content".getBytes());
        String result = movieService.storeFile(file);
        assertThat(result).startsWith("/uploads/").doesNotContain(".");
    }

    @Test
    void shouldHandleFileWithNullOriginalFilename() {
        MockMultipartFile file = new MockMultipartFile("file", null, "image/jpeg", "content".getBytes());
        String result = movieService.storeFile(file);
        assertThat(result).startsWith("/uploads/");
    }

    @Test
    void shouldProcessGalleryWithMixedEmptyFiles() {
        Movie movie = Movie.builder().title("Test").build();
        MockMultipartFile validFile = new MockMultipartFile("gallery", "valid.jpg", "image/jpeg", "content".getBytes());
        MockMultipartFile emptyFile = new MockMultipartFile("gallery", "", "image/jpeg", new byte[0]);

        when(movieRepository.save(movie)).thenReturn(movie);

        movieService.addMovie(movie, null, List.of(validFile, emptyFile));

        assertThat(movie.getImages()).hasSize(1);
        assertThat(movie.getImages().get(0).getImageUrl()).startsWith("/uploads/").endsWith(".jpg");
    }

    @Test
    void shouldHandleProcessActorsWithNullAndEmptyString() {
        Movie movie1 = new Movie();
        when(movieRepository.save(movie1)).thenReturn(movie1);
        movieService.addMovie(movie1, null, null);
        assertThat(movie1.getActors()).isEmpty();

        Movie movie2 = new Movie();
        when(movieRepository.save(movie2)).thenReturn(movie2);
        movieService.addMovie(movie2, "   ", null);
        assertThat(movie2.getActors()).isEmpty();
    }

    @Test
    void shouldHandleProcessActorsWithEmptySegments() {
        Movie movie = new Movie();
        when(actorRepository.findByName(any())).thenReturn(Optional.empty());
        when(actorRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(movieRepository.save(movie)).thenReturn(movie);

        movieService.addMovie(movie, "Actor1, , Actor2", null);

        assertThat(movie.getActors()).hasSize(2);
        assertThat(movie.getActors()).extracting("name").containsExactly("Actor1", "Actor2");
    }

    @Test
    void shouldReturnNullWhenStoringEmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "text/plain", new byte[0]);
        String result = movieService.storeFile(emptyFile);
        assertThat(result).isNull();
    }

    @Test
    void shouldCreateUploadDirectoryIfNotExists() throws IOException {
        Path uploadPath = Paths.get("uploads");
        if (Files.exists(uploadPath)) {
            Files.walk(uploadPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());
        movieService.storeFile(file);

        assertThat(Files.exists(uploadPath)).isTrue();
    }

    @Test
    void shouldPreserveExtensionWhenStoringFile() {
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", "content".getBytes());
        String result = movieService.storeFile(file);
        assertThat(result).endsWith(".png");
    }

    @Test
    void shouldDeleteSelectedGalleryImages() {
        Long id = 1L;
        Movie movie = Movie.builder().id(id).title("Test").build();
        MovieImage img1 = MovieImage.builder().id(10L).imageUrl("/uploads/delete_me.jpg").movie(movie).build();
        MovieImage img2 = MovieImage.builder().id(20L).imageUrl("/uploads/keep_me.jpg").movie(movie).build();
        movie.getImages().add(img1);
        movie.getImages().add(img2);

        when(movieRepository.findById(id)).thenReturn(Optional.of(movie));
        when(movieRepository.save(movie)).thenReturn(movie);

        movieService.updateMovie(id, movie, null, null, List.of(10L));

        assertThat(movie.getImages()).hasSize(1);
        assertThat(movie.getImages().get(0).getId()).isEqualTo(20L);
    }

    @Test
    void shouldLogWarningWhenFileDeletionFails() throws IOException {
        Long id = 1L;
        Movie movie = Movie.builder().id(id).title("Test").build();
        String failingPath = "/uploads/fail_delete.jpg";
        MovieImage img = MovieImage.builder().id(10L).imageUrl(failingPath).movie(movie).build();
        movie.getImages().add(img);

        when(movieRepository.findById(id)).thenReturn(Optional.of(movie));
        when(movieRepository.save(movie)).thenReturn(movie);

        Path path = Paths.get("." + failingPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        Path blocker = path.resolve("blocker");
        Files.createFile(blocker);

        try {
            movieService.updateMovie(id, movie, null, null, List.of(10L));
            assertThat(movie.getImages()).isEmpty();
        } finally {
            Files.deleteIfExists(blocker);
            Files.deleteIfExists(path);
        }
    }
}