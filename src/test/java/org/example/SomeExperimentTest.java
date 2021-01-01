package org.example;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class SomeExperimentTest {

    @Test
    public void falseWin() throws InterruptedException {
        Flux<Tuple2<Boolean, Long>> flux = Flux.just(true, true, false, true)
                .sort()
                .groupBy(x -> x)
                .flatMap(group -> Mono.zip(Mono.just(group.key()), group.count()));

        StepVerifier.create(flux.log())
                .expectNext(Tuples.of(Boolean.FALSE, 1L))
                .expectNext(Tuples.of(Boolean.TRUE, 3L))
                .verifyComplete();

        Flux<Tuple2<Boolean, Long>> flux2 = Flux.just(true, true, true, true)
                .sort()
                .groupBy(x -> x)
                .flatMap(group -> Mono.zip(Mono.just(group.key()), group.count()));

        StepVerifier.create(flux2.log())
                .expectNext(Tuples.of(Boolean.TRUE, 4L))
                .verifyComplete();

        Mono<Boolean> flux3 = Flux.just(true, true, true, true)
                .sort()
                .groupBy(x -> x)
                .flatMap(group -> Mono.zip(Mono.just(group.key()), group.count()))
                .collectList()
                .map(list -> {
                    for (Tuple2<Boolean, Long> objects : list) {
                        if (!objects.getT1()) {
                            return false;
                        }
                    }
                    return true;
                });

        StepVerifier.create(flux3.log())
                .expectNext(true)
                .verifyComplete();

    }

    @Test
    public void withDelay() throws InterruptedException {
        Mono<Boolean> flux4 = Flux.just(true, true, true, true)
                .concatWith(Flux.just(true, true, false, true))
                .sort()
                .groupBy(x -> x)
                .flatMap(group -> Mono.zip(Mono.just(group.key()), group.count()))
                .collectList()
                .map(list -> {
                    for (Tuple2<Boolean, Long> objects : list) {
                        if (!objects.getT1()) {
                            return false;
                        }
                    }
                    return true;
                });

        StepVerifier.create(flux4.log())
                .expectNext(false)
                .verifyComplete();

        Thread.sleep(3000);

    }
}
