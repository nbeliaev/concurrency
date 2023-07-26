package course.concurrency.m5_streams;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ThreadPoolTaskTests {

    private ThreadPoolTask task = new ThreadPoolTask();
    private ThreadPoolExecutor lifoExecutor = task.getLifoExecutor();
    private ThreadPoolExecutor rejectExecutor = task.getRejectExecutor();

    @Test
    void shouldProcessInLifoOrder() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        int elementsCount = 10;
        lifoExecutor.setCorePoolSize(1);

        Queue<Integer> processed = new LinkedBlockingQueue<>();
        for (int i = 0; i < elementsCount; i++) {
            final int value = i;
            lifoExecutor.execute(
                    () -> {
                        try {
                            latch.await();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        processed.add(value);
                    });

        }
        latch.countDown();
        lifoExecutor.shutdown();
        lifoExecutor.awaitTermination(2, TimeUnit.SECONDS);

        List<Integer> expectedResult = IntStream.range(1, elementsCount).boxed()
                .sorted(Collections.reverseOrder()).collect(Collectors.toList());
        expectedResult.add(0, 0);

        for (Integer expected : expectedResult) {
            Integer value = processed.poll();
            assertEquals(expected, value);
        }
    }

    @Test
    void shouldDiscardIfNoAvailableThreads() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Queue<Integer> processed = new LinkedBlockingQueue<>();
        int poolSize = 16;
        rejectExecutor.setMaximumPoolSize(poolSize);
        rejectExecutor.setCorePoolSize(poolSize);

        for (int i = 0; i < poolSize*2; i++) {
            final int value = i;
            rejectExecutor.execute(
                    () -> {
                        try {
                            latch.await();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        processed.add(value);
                    });
        }
        latch.countDown();

        rejectExecutor.shutdown();
        rejectExecutor.awaitTermination(2, TimeUnit.SECONDS);
        
        assertEquals(poolSize, processed.size(), "Number of processed elements doesn't equals pool size");

        List<Integer> expectedResult = IntStream.range(0, poolSize).boxed().collect(Collectors.toList());
        for (int i = 0; i < processed.size(); i++) {
            assertEquals(processed.poll(), expectedResult.get(i));
        }
    }
}
