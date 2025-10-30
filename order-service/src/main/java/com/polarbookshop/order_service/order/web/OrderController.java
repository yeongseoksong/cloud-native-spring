package com.polarbookshop.order_service.order.web;


import com.polarbookshop.order_service.order.domain.Order;
import com.polarbookshop.order_service.order.domain.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("orders")
public class OrderController {
    private final OrderService orderService;
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Flux<Order> geAllOrders(){
        return orderService.getAllOrders();
    }

    @PostMapping
    public Mono<Order> submitOrder(@Valid @RequestBody OrderRequest orderRequest){
        return orderService.submitOrder(orderRequest.isbn(),orderRequest.quantity());
    }
}
