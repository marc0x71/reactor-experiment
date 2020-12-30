package org.example;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class FluxEmitterTest {

    @Test
    public void one() {
        FluxEmitter<Integer> fluxEmitter = new FluxEmitter<>();

        fluxEmitter.next(1);
        fluxEmitter.next(2);
        fluxEmitter.next(3);
        fluxEmitter.complete();

        Flux<Integer> flux = fluxEmitter.attach();

        StepVerifier.create(flux.log())
                .expectNext(1, 2, 3)
                .verifyComplete();
    }

    @Test
    public void two() {
        FluxEmitter<Integer> fluxEmitter = new FluxEmitter<>();

        fluxEmitter.next(1);
        fluxEmitter.next(2);
        fluxEmitter.next(3);
        fluxEmitter.complete();

        Flux<Integer> flux = fluxEmitter.attach();

        StepVerifier.create(flux.log())
                .expectNext(1, 2, 3)
                .verifyComplete();

        StepVerifier.create(flux.log())
                .expectNext(1, 2, 3)
                .verifyComplete();
    }

}