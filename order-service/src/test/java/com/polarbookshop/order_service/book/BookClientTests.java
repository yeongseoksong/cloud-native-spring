package com.polarbookshop.order_service.book;


import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

public class BookClientTests {
    private MockWebServer mockWebServer;
    private BookClient bookClient;

    @BeforeEach
    void setup() throws IOException{
        this.mockWebServer = new MockWebServer();
        this.mockWebServer.start();
        var webClient= WebClient.builder().baseUrl(this.mockWebServer.url("/").uri().toString()).build();
        this.bookClient = new BookClient(webClient);
    }

    @AfterEach
    void clean() throws IOException{
        this.mockWebServer.shutdown();
    }

    @Test
    void whenBookExistingThenReturnBook(){
        var isbn="1234567890";

       var resp= new MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                            "isbn": %s,
                            "title": "Title",
                            "author": "Author",
                            "price": 9.90,
                            "publisher": "Polarsophia"
                        }
                        """.formatted(isbn));
        mockWebServer.enqueue(resp);

        Mono<Book> book = bookClient.getBookByIsbn(isbn);

        StepVerifier.create(book).expectNextMatches(
                b->b.isbn().equals(isbn))
                .verifyComplete();
    }
}
