package com.cinema.booking.controller;

import com.cinema.booking.config.SecurityConfig;
import com.cinema.booking.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileController.class)
@Import(SecurityConfig.class)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void shouldReturnNotFoundForNonExistentFile() throws Exception {
        mockMvc.perform(get("/uploads/non-existent-file.jpg"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void shouldServeFileIfExists() throws Exception {
        Path uploadDir = Paths.get("uploads");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        Path testFile = uploadDir.resolve("test-image.jpg");
        Files.write(testFile, "test content".getBytes());

        try {
            mockMvc.perform(get("/uploads/test-image.jpg"))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Content-Disposition"));
        } finally {
            Files.deleteIfExists(testFile);
        }
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestForInvalidPath() throws Exception {
        mockMvc.perform(get("/uploads/test%00.jpg"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldTriggerBadRequestForMalformedUrl() throws Exception {
        mockMvc.perform(get("/uploads/.."))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleInternalExceptionDirectly() {
        FileController controller = new FileController();
        ResponseEntity<Resource> response = controller.serveFile(null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}