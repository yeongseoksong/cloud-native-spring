package com.polarbookshop.order_service.order.domain;


import com.polarbookshop.order_service.book.Book;
import com.polarbookshop.order_service.book.BookClient;
import com.polarbookshop.order_service.order.event.OrderAcceptedMessage;
import com.polarbookshop.order_service.order.event.OrderDispatchedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final BookClient bookClient;
    private final StreamBridge streamBridge;

    public OrderService(OrderRepository orderRepository, BookClient bookClient,StreamBridge streamBridge) {
        this.orderRepository = orderRepository;
        this.bookClient = bookClient;
        this.streamBridge = streamBridge;
    }


    public Flux<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Flux<Order>getAllOrders(String userId) {
        return orderRepository.findAllByCreatedBy(userId);
    }

    @Transactional
    public Mono<Order> submitOrder(String isbn,int quantity){
        return bookClient.getBookByIsbn(isbn)
                .map(book -> buildAcceptedOrder(book,quantity))
                .defaultIfEmpty(buildRejectedOrder(isbn, quantity))
                .flatMap(orderRepository::save)
                .doOnNext(this::publishOrderAcceptedEvent);
    }

    public static Order buildRejectedOrder(String isbn,int quantity){
        return Order.of(isbn,null,null,quantity,OrderStatus.REJECTED);
    }

    public static Order buildAcceptedOrder(Book book, int quantity) {
        return Order.of(book.isbn(), book.title() + " - " + book.author(),
                book.price(), quantity, OrderStatus.ACCEPTED);
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
                order.version(),
                order.createdBy(),
                order.lastModifiedBy()
        );
    }



    private void publishOrderAcceptedEvent(Order order) {
        if(!order.status().equals(OrderStatus.ACCEPTED)){
            return;
        }
        OrderAcceptedMessage orderAcceptedMessage = new OrderAcceptedMessage(order.id());

        log.info("Sending order accepted event with id: {}",order.id());

        boolean result = streamBridge.send("acceptOrder-out-0", orderAcceptedMessage);

        log.info("Result of sending data for order with id {} : {}", order.id(), result);

    }
}
