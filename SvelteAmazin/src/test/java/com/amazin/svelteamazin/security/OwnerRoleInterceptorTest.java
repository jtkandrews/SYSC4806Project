package com.amazin.svelteamazin.security;

import com.amazin.svelteamazin.controller.BookController;
import com.amazin.svelteamazin.model.Book;
import com.amazin.svelteamazin.model.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OwnerRoleInterceptorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;


    @BeforeEach
    void clean() {
        bookRepository.deleteAll();
    }

    @Test
    void createBook_withoutOwnerCookie_isForbidden() throws Exception {
        String json = """
            {
              "isbn": "9780000000010",
              "title": "Forbidden Book",
              "author": "Hackerman",
              "price": 10.0,
              "inventory": 1
            }
            """;

        mockMvc.perform(
                        post("/api/owner/books")   // or whatever your owner-only URL is
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void createBook_withOwnerCookie_isAllowed() throws Exception {
        String json = """
            {
              "isbn": "9780000000011",
              "title": "Owner Book",
              "author": "Legit Owner",
              "price": 12.0,
              "inventory": 2
            }
            """;

        Cookie ownerCookie = new Cookie("role", "OWNER");

        mockMvc.perform(
                        post("/api/owner/books")
                                .cookie(ownerCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated());
    }
}
