package com.polarbookshop.order_service.order.web;


import com.polarbookshop.order_service.order.domain.Order;
import com.polarbookshop.order_service.order.domain.OrderService;
import com.polarbookshop.order_service.order.domain.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@WebFluxTest(OrderController.class)
public class OrderControllerWebFluxTests {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private OrderService orderService;

    @Test
    void whenBookNotAvailableThenRejectOrder(){
        OrderRequest orderRequest = new OrderRequest("1234567890", 3);
        Order expectedOrder = OrderService.buildRejectedOrder(
                orderRequest.isbn(), orderRequest.quantity()
        );

        given(orderService.submitOrder(orderRequest.isbn(),orderRequest.quantity())).willReturn(Mono.just(expectedOrder));

        webTestClient.post().uri("/orders")
                .bodyValue(orderRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Order.class).value(o->{
                    assertThat(o).isNotNull();
                    assertThat(o.status()).isEqualTo(OrderStatus.REJECTED);
                });
    }

}
