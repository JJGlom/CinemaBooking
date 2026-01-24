package com.cinema.booking.controller;

import com.cinema.booking.model.Movie;
import com.cinema.booking.service.MovieService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminMovieController.class)
class AdminMovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieService movieService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowListMovies() throws Exception {
        when(movieService.getAllMovies()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/movies"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/movies"))
                .andExpect(model().attributeExists("movies"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowAddForm() throws Exception {
        mockMvc.perform(get("/admin/movies/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/movie-form"))
                .andExpect(model().attributeExists("movie"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowEditForm() throws Exception {
        Movie movie = Movie.builder().id(1L).title("Test").build();
        when(movieService.getMovieById(1L)).thenReturn(movie);

        mockMvc.perform(get("/admin/movies/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/movie-form"))
                .andExpect(model().attributeExists("movie"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldSaveNewMovie() throws Exception {
        mockMvc.perform(multipart("/admin/movies/save")
                        .param("title", "New Movie")
                        .param("genre", "Action")
                        .param("director", "Director")
                        .param("durationMinutes", "120")
                        .param("ageRestriction", "12")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/movies"));

        verify(movieService).addMovie(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateExistingMovie() throws Exception {
        mockMvc.perform(multipart("/admin/movies/save")
                        .param("id", "1")
                        .param("title", "Updated Movie")
                        .param("genre", "Action")
                        .param("director", "Director")
                        .param("durationMinutes", "120")
                        .param("ageRestriction", "12")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        verify(movieService).updateMovie(eq(1L), any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldSaveMovieWithImage() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "content".getBytes());
        when(movieService.storeFile(any())).thenReturn("/uploads/test.jpg");

        mockMvc.perform(multipart("/admin/movies/save")
                        .file(image)
                        .param("title", "Movie with Img")
                        .param("genre", "Action")
                        .param("director", "Director")
                        .param("durationMinutes", "120")
                        .param("ageRestriction", "12")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldIgnoreEmptyImageFile() throws Exception {
        MockMultipartFile emptyImage = new MockMultipartFile("image", "", "image/jpeg", new byte[0]);

        mockMvc.perform(multipart("/admin/movies/save")
                        .file(emptyImage)
                        .param("title", "Test")
                        .param("genre", "Genre")
                        .param("director", "Dir")
                        .param("durationMinutes", "100")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        verify(movieService).addMovie(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldIgnoreEmptyGalleryFiles() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("gallery", "", "image/jpeg", new byte[0]);

        mockMvc.perform(multipart("/admin/movies/save")
                        .file(emptyFile)
                        .param("title", "Test")
                        .param("genre", "Genre")
                        .param("director", "Dir")
                        .param("durationMinutes", "100")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectWhenImageContentTypeIsNull() throws Exception {
        MockMultipartFile fileWithoutType = new MockMultipartFile("image", "test.jpg", null, "content".getBytes());

        mockMvc.perform(multipart("/admin/movies/save")
                        .file(fileWithoutType)
                        .param("title", "Test")
                        .param("genre", "Genre")
                        .param("director", "Dir")
                        .param("durationMinutes", "100")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/movie-form"))
                .andExpect(model().hasErrors());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectWhenImageContentTypeIsNotImage() throws Exception {
        MockMultipartFile textFile = new MockMultipartFile("image", "test.txt", "text/plain", "content".getBytes());

        mockMvc.perform(multipart("/admin/movies/save")
                        .file(textFile)
                        .param("title", "Test")
                        .param("genre", "Genre")
                        .param("director", "Dir")
                        .param("durationMinutes", "100")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectWhenGalleryFileContentTypeIsNull() throws Exception {
        MockMultipartFile fileWithoutType = new MockMultipartFile("gallery", "test.jpg", null, "content".getBytes());

        mockMvc.perform(multipart("/admin/movies/save")
                        .file(fileWithoutType)
                        .param("title", "Test")
                        .param("genre", "Genre")
                        .param("director", "Dir")
                        .param("durationMinutes", "100")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectWhenGalleryFileContentTypeIsNotImage() throws Exception {
        MockMultipartFile textFile = new MockMultipartFile("gallery", "test.txt", "text/plain", "content".getBytes());

        mockMvc.perform(multipart("/admin/movies/save")
                        .file(textFile)
                        .param("title", "Test")
                        .param("genre", "Genre")
                        .param("director", "Dir")
                        .param("durationMinutes", "100")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnErrorsWhenValidationFails() throws Exception {
        mockMvc.perform(multipart("/admin/movies/save")
                        .param("title", "")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/movie-form"))
                .andExpect(model().hasErrors());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteMovie() throws Exception {
        mockMvc.perform(get("/admin/movies/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/movies"));
        verify(movieService).deleteMovie(1L);
    }
}