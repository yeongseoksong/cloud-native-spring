package com.porlarbookshop.catalogservice.web;

import com.porlarbookshop.catalogservice.config.SecurityConfig;
import com.porlarbookshop.catalogservice.domain.BookNotFoundException;
import com.porlarbookshop.catalogservice.domain.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
class BookControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Test
    void whenGetBookNotExistingThenShouldReturn404() throws Exception {
        String isbn="73737313940";

        given(bookService.viewBookDetails(isbn)).willThrow(BookNotFoundException.class);

        mockMvc.perform(get("/books/"+isbn)).andExpect(status().isNotFound());
    }

    @Test
    void whenDeleteBookWithEmployeeRoleThenShouldReturn204() throws Exception {
        String isbn="73737313940";
        mockMvc.perform(MockMvcRequestBuilders.delete("/books/"+isbn)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_employee"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void whenDeleteBookWithCustomerRoleThenShouldReturn403() throws Exception {
        String isbn="73737313940";
        mockMvc.perform(MockMvcRequestBuilders.delete("/books/"+isbn)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_customer"))))
                .andExpect(status().isForbidden());
    }
    @Test
    void whenDeleteBookNotAuthenticatedThenShouldReturn401() throws Exception {
        String isbn="73737313940";
        mockMvc.perform(MockMvcRequestBuilders.delete("/books/"+isbn))
                .andExpect(status().isUnauthorized());
    }

}