package course.concurrency.m3_shared.collections;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import static java.util.stream.Collectors.toSet;

public class RestaurantService {

    private static final String STAT_SPLITTER = " - ";

    private final Map<String, Restaurant> restaurantMap = new ConcurrentHashMap<>() {{
        put("A", new Restaurant("A"));
        put("B", new Restaurant("B"));
        put("C", new Restaurant("C"));
    }};

    private final Map<String, LongAdder> stat = new ConcurrentHashMap<>() {{
        put("A", new LongAdder());
        put("B", new LongAdder());
        put("C", new LongAdder());
    }};

    public Restaurant getByName(String restaurantName) {
        addToStat(restaurantName);
        return restaurantMap.get(restaurantName);
    }

    public void addToStat(String restaurantName) {
        stat.get(restaurantName).increment();
        //stat.merge(restaurantName, 1, (o, n) -> o);
    }

    public Set<String> printStat() {
        return stat.entrySet().parallelStream()
                .map(entry -> entry.getKey() + STAT_SPLITTER + entry.getValue().sum())
                .collect(toSet());
    }
}
