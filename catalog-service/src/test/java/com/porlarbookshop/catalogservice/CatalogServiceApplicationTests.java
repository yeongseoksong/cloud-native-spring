package com.porlarbookshop.catalogservice;

import com.porlarbookshop.catalogservice.domain.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class CatalogServiceApplicationTests {

    @Autowired private WebTestClient webTestClient;

    @Test
    void contextLoads() {

        var expected= Book.of("1231231231","Title","Author",9.90);

        webTestClient.post().uri("/books")
                .bodyValue(expected)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Book.class).value(b->{
                    assertThat(b).isNotNull();
                    assertThat(b.isbn()).isEqualTo(expected.isbn());
                });
    }

}
