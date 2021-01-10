package org.example;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static java.util.Arrays.asList;

public class MixedTest {

    @Test
    public void mono2Flux() {
        List<Integer> integers = List.of(1, 2, 3, 4, 5);
        Flux<Integer> flux = Mono.just(integers)
                .flatMapIterable(x -> x);

        StepVerifier.create(flux.log())
                .expectNext(1)
                .expectNext(2)
                .expectNext(3)
                .expectNext(4)
                .expectNext(5)
                .verifyComplete();
    }

    @Test
    public void mono2Flux2Mono() {
        List<Integer> integers = List.of(1, 2, 3, 4, 5);
        Mono<List<Integer>> mono = Mono.just(integers)
                .flatMapIterable(x -> x)
                .map(x -> x * 2)
                .collectList();

        StepVerifier.create(mono.log())
                .expectNext(asList(2, 4, 6, 8, 10))
                .verifyComplete();
    }
}
