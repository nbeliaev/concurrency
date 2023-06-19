package course.concurrency.m2_async.cf.min_price;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class PriceAggregator {

    private static final int PRICE_RETRIEVING_TIMEOUT = 2900;
    private PriceRetriever priceRetriever = new PriceRetriever();

    public void setPriceRetriever(PriceRetriever priceRetriever) {
        this.priceRetriever = priceRetriever;
    }

    private Collection<Long> shopIds = Set.of(10l, 45l, 66l, 345l, 234l, 333l, 67l, 123l, 768l);

    public void setShops(Collection<Long> shopIds) {
        this.shopIds = shopIds;
    }

    public double getMinPrice(long itemId) {
        Executor executor;
        int taskLimit = 100;
        boolean isTooMuchTasks = shopIds.size() > taskLimit;
        if (isTooMuchTasks) {
            executor = Executors.newFixedThreadPool(shopIds.size());
        } else {
            executor = Executors.newCachedThreadPool();
        }

        List<CompletableFuture<Double>> pricesResult = shopIds.stream()
                .map(shopId -> supplyAsync(() -> priceRetriever.getPrice(itemId, shopId), executor)
                        .completeOnTimeout(Double.NaN, PRICE_RETRIEVING_TIMEOUT, TimeUnit.MILLISECONDS)
                        .exceptionally(ex -> Double.NaN))
                .collect(Collectors.toList());

        return pricesResult.stream()
                .map(CompletableFuture::join)
                .min(Comparator.comparingDouble(i -> i))
                .orElse(Double.NaN);
    }
}
