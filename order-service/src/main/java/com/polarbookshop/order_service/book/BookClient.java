package com.polarbookshop.order_service.book;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class BookClient {
    private static final String BOOK_ROOT_API="/books/";
    private final WebClient webClient;

    public BookClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<Book> getBookByIsbn(String isbn){
        return webClient
                .get()
                .uri(BOOK_ROOT_API + isbn)
                .retrieve()
                .bodyToMono(Book.class).
                // timeout -> retry  각 재시도마다 time out 적용 , retry-> timeout 전체 재시도를 포함해 time out 적용
                timeout(Duration.ofSeconds(3),Mono.empty())
                .onErrorResume(WebClientResponseException.NotFound.class, e->Mono.empty()) //404 에러 fallback
                .retryWhen(
                        Retry.backoff(3,Duration.ofMillis(100))
                ).onErrorResume(Exception.class, (e)->Mono.empty()); // 3회 재시도 후에도 에러 fallback
    }
}
