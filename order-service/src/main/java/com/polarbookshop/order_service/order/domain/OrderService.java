package com.polarbookshop.order_service.order.domain;


import com.polarbookshop.order_service.book.Book;
import com.polarbookshop.order_service.book.BookClient;
import com.polarbookshop.order_service.order.event.OrderDispatchedMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final BookClient bookClient;

    public OrderService(OrderRepository orderRepository, BookClient bookClient) {
        this.orderRepository = orderRepository;
        this.bookClient = bookClient;
    }


    public Flux<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Mono<Order> submitOrder(String isbn,int quantity){
        return bookClient.getBookByIsbn(isbn)
                .map(book -> buildAcceptedOrder(book,quantity))
                .defaultIfEmpty(buildRejectedOrder(isbn, quantity))
                .flatMap(orderRepository::save);
    }

    public static Order buildRejectedOrder(String isbn,int quantity){
        return Order.of(isbn,null,null,quantity,OrderStatus.REJECTED);
    }

    public static Order buildAcceptedOrder(Book book, int quantity) {
        return null;
    }

    public Flux<Order> consumeOrderDispatchedEvent(Flux<OrderDispatchedMessage> flux) {
        return flux.flatMap(m-> orderRepository.findById(m.orderId()))
                        .map(this::buildDispatchedOrder)
                        .flatMap(orderRepository::save);
    }

    private Order buildDispatchedOrder(Order order) {
        return new Order(
                order.id(),
                order.bookIsbn(),
                order.bookName(),
                order.bookPrice(),
                order.quantity(),
                OrderStatus.DISPATCHED,
                order.createdDate(),
                order.lastModifiedDate(),
                order.version()
        );
    }
}
