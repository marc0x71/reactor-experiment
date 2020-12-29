package org.example;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;

public class OperatorsTest {

    private static final Logger logger = LoggerFactory.getLogger(OperatorsTest.class);

    @Test
    public void merge() {
        Flux<String> flux1 = Flux.just("A", "B", "C");
        Flux<String> flux2 = Flux.just("1", "2", "3");

        Flux<String> flux = Flux.merge(flux1, flux2);

        StepVerifier.create(flux.log())
                .expectNext("A", "B", "C")
                .expectNext("1", "2", "3")
                .verifyComplete();
    }

    @Test
    public void concat() {
        Flux<String> flux1 = Flux.just("A", "B", "C");
        Flux<String> flux2 = Flux.just("1", "2", "3");

        Flux<String> flux = Flux.concat(flux1, flux2);

        StepVerifier.create(flux.log())
                .expectNext("A", "B", "C")
                .expectNext("1", "2", "3")
                .verifyComplete();
    }

    @Test
    public void mergeWithDelay() {
        Flux<String> flux1 = Flux.just("A", "B", "C").delayElements(Duration.ofMillis(500));
        Flux<String> flux2 = Flux.just("D", "E", "F").delayElements(Duration.ofMillis(500));

        Flux<String> flux = Flux.merge(flux1, flux2);

        // merge doesn't guarantee the sequence of elements using 'delayElements' (see concatWithDelay test)
        // BUT is more faster than concatWithDelay test!
        long start = System.currentTimeMillis();
        StepVerifier.create(flux.log())
                .expectNextCount(6)
                //.expectNext("A", "B", "C")
                //.expectNext("D", "E", "F")
                .verifyComplete();
        logger.info("elapsed = {}", (System.currentTimeMillis() - start));
    }

    @Test
    public void concatWithDelay() {
        Flux<String> flux1 = Flux.just("A", "B", "C").delayElements(Duration.ofMillis(500));
        Flux<String> flux2 = Flux.just("D", "E", "F").delayElements(Duration.ofMillis(500));

        Flux<String> flux = Flux.concat(flux1, flux2);

        // But concat guarantee the sequence of elements also using 'delayElements'
        // BUT is more slower than mergeWithDelay test!
        long start = System.currentTimeMillis();
        StepVerifier.create(flux.log())
                .expectNext("A", "B", "C")
                .expectNext("D", "E", "F")
                .verifyComplete();
        logger.info("elapsed = {}", (System.currentTimeMillis() - start));
    }

    @Test
    public void concatWith() {
        Flux<String> flux = Flux.just("A", "B", "C")
                .concatWith(Flux.just("1", "2", "3"));

        StepVerifier.create(flux.log())
                .expectNext("A", "B", "C")
                .expectNext("1", "2", "3")
                .verifyComplete();
    }

    @Test
    public void zipDifferentElementsType() {
        Flux<String> strings = Flux.just("A", "B", "C");
        Flux<Integer> numbers = Flux.just(1, 2, 3);

        Flux<Tuple2<String, Integer>> result = Flux.zip(strings, numbers);

        StepVerifier.create(result.log())
                .expectNext(Tuples.of("A", 1))
                .expectNext(Tuples.of("B", 2))
                .expectNext(Tuples.of("C", 3))
                .verifyComplete();
    }

    @Test
    public void filter() {
        Flux<Integer> flux = Flux.range(0, 10)
                .filter(x -> x % 2 == 0);

        StepVerifier.create(flux.log())
                .expectNext(0, 2, 4, 6, 8)
                .verifyComplete();
    }

    @Test
    public void map() {
        Flux<Double> flux = Flux.range(1, 5)
                .map(integer -> (double) (integer * integer));

        StepVerifier.create(flux.log())
                .expectNext(1.0, 4.0, 9.0, 16.0, 25.0)
                .verifyComplete();
    }

    @Test
    public void cache() {
        Flux flux = Flux.range(1, 5).delayElements(Duration.ofMillis(500)).cache();

        StepVerifier.create(flux.log())
                .expectNextCount(5)
                .verifyComplete();

        StepVerifier.create(flux.log())
                .expectNextCount(5)
                .verifyComplete();
    }

    @Test
    public void virtualizeTimeExecutionTest() {
        VirtualTimeScheduler.getOrSet();
        Flux flux = Flux.range(1, 5).delayElements(Duration.ofMillis(500));

        StepVerifier.withVirtualTime(() -> flux.log())
                .thenAwait(Duration.ofSeconds(3)) // <-- must be greater than normal test execution time
                .expectNextCount(5)
                .verifyComplete();
    }

}
