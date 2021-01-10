package org.example;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;

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

    @Test
    public void sequence() {

        Mono<Integer> mono = Mono.just(1).subscribeOn(Schedulers.boundedElastic())
                .map(x -> x == 1 ? 2 : x)
                .map(x -> x == 2 ? 3 : x)
                .delaySubscription(Duration.ofSeconds(1))
                .map(x -> x == 3 ? 4 : x)
                .map(x -> x == 4 ? 5 : x);

        StepVerifier.create(mono.log())
                .expectNext(5)
                .verifyComplete();
    }
}
