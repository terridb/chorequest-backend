package com.terridb.chorequest.exceptions;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void returns409_whenDataIntegrityIsViolated_withoutLeakingSql() throws Exception {
        mockMvc.perform(get("/test/integrity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Request conflicts with existing data"))
                .andExpect(content().string(not(containsString("duplicate key"))));
    }

    @Test
    void returns409_whenOptimisticLockFails() throws Exception {
        mockMvc.perform(get("/test/optimistic-lock"))
                .andExpect(status().isConflict());
    }

    @Test
    void returns400_whenJsonIsMalformed() throws Exception {
        mockMvc.perform(post("/test/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns400_whenPathVariableIsNotAUuid() throws Exception {
        mockMvc.perform(get("/test/uuid/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns400_withFieldErrors_whenValidationFails() throws Exception {
        mockMvc.perform(post("/test/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void returns500_withoutLeakingDetails_whenExceptionIsUnexpected() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred"))
                .andExpect(content().string(not(containsString("secret internal detail"))))
                .andExpect(content().string(not(containsString("at com.terridb"))));
    }

    @Test
    void keepsSpringStatus_whenMethodIsNotAllowed() throws Exception {
        mockMvc.perform(put("/test/unexpected"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void keepsSpringStatus_whenContentTypeIsUnsupported() throws Exception {
        mockMvc.perform(post("/test/body")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("hello"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void stillHandlesCustomExceptions() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Thing not found"));
    }

    @RestController
    static class TestController {
        @GetMapping("/test/integrity")
        void integrity() {
            throw new DataIntegrityViolationException("duplicate key value violates unique constraint \"users_auth0_sub_key\"");
        }

        @GetMapping("/test/optimistic-lock")
        void optimisticLock() {
            throw new OptimisticLockingFailureException("Row was updated by another transaction");
        }

        @GetMapping("/test/uuid/{id}")
        void uuid(@PathVariable UUID id) {
        }

        @PostMapping("/test/body")
        void body(@Valid @RequestBody TestBody body) {
        }

        @GetMapping("/test/unexpected")
        void unexpected() {
            throw new IllegalStateException("secret internal detail");
        }

        @GetMapping("/test/not-found")
        void notFound() {
            throw new RecordNotFoundException("Thing not found");
        }
    }

    record TestBody(@NotBlank String name) {
    }
}
