package org.example;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
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
}
