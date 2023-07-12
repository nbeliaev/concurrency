package course.concurrency.m3_shared.collections;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toSet;

public class RestaurantService {

    private static final String STAT_SPLITTER = " - ";

    private Map<String, Restaurant> restaurantMap = new ConcurrentHashMap<>() {{
        put("A", new Restaurant("A"));
        put("B", new Restaurant("B"));
        put("C", new Restaurant("C"));
    }};

    private Map<String, Integer> stat = new ConcurrentHashMap<>();

    public Restaurant getByName(String restaurantName) {
        addToStat(restaurantName);
        return restaurantMap.get(restaurantName);
    }

    public void addToStat(String restaurantName) {
        stat.merge(restaurantName, 1, Integer::sum);
    }

    public Set<String> printStat() {
        return stat.entrySet().parallelStream()
                .map(entry -> entry.getKey() + STAT_SPLITTER + entry.getValue())
                .collect(toSet());
    }
}
