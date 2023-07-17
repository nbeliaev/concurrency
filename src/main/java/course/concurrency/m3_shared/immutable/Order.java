package course.concurrency.m3_shared.immutable;

import java.util.Collections;
import java.util.List;

import static course.concurrency.m3_shared.immutable.Order.Status.*;

public final class Order {

    public enum Status { NEW, IN_PROGRESS, DELIVERED }

    private final Long id;
    private final List<Item> items;
    private PaymentInfo paymentInfo;
    private boolean isPacked;
    private Status status;

    public Order(Long id, List<Item> items) {
        this.id = id;
        this.items = items;
        this.status = NEW;
    }

    private Order(Long id, List<Item> items, PaymentInfo paymentInfo, boolean isPacked, Status status) {
        this.id = id;
        this.items = items;
        this.paymentInfo = paymentInfo;
        this.isPacked = isPacked;
        this.status = status;
    }

    public boolean checkStatus() {
        return paymentInfo != null && isPacked;
    }

    public Long getId() {
        return id;
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    public PaymentInfo getPaymentInfo() {
        return paymentInfo;
    }

    public Order paid(PaymentInfo paymentInfo) {
        return new Order(id, items, paymentInfo, isPacked, IN_PROGRESS);
    }

    public boolean isPacked() {
        return isPacked;
    }

    public Order packed() {
        return new Order(id, items, paymentInfo, true, IN_PROGRESS);
    }

    public Status getStatus() {
        return status;
    }

    public Order delivered() {
        if (isDelivered()) {
            throw new IllegalStateException(String.format("Order %s is already delivered", id));
        }
        return new Order(id, items, paymentInfo, isPacked, DELIVERED);
    }

    public boolean isDelivered() {
        return status == DELIVERED;
    }
}
