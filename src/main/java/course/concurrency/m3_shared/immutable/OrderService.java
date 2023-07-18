package course.concurrency.m3_shared.immutable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class OrderService {

    private final Map<Long, Order> currentOrders = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong();

    private long nextId() {
        return nextId.getAndIncrement();
    }

    public long createOrder(List<Item> items) {
        long id = nextId();
        Order order = new Order(id, items);
        currentOrders.put(id, order);
        return id;
    }

    public void updatePaymentInfo(long orderId, PaymentInfo paymentInfo) {
        currentOrders.computeIfPresent(orderId, (k, order) -> order.paid(paymentInfo));
        if (currentOrders.get(orderId).checkStatus()) {
            deliver(currentOrders.get(orderId));
        }
    }

    public void setPacked(long orderId) {
        currentOrders.computeIfPresent(orderId, (k, order) -> order.packed());
        if (currentOrders.get(orderId).checkStatus()) {
            deliver(currentOrders.get(orderId));
        }
    }

    private void deliver(Order order) {
        /* ... */
        currentOrders.computeIfPresent(order.getId(), (k, o) -> {
            if (o.isDelivered()) {
                throw new IllegalStateException(String.format("Order %s is already delivered", order.getId()));
            }
            return o.delivered();
        });
    }

    public boolean isDelivered(long orderId) {
        return currentOrders.get(orderId).isDelivered();
    }
}
