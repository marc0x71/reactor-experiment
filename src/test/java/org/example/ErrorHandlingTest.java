package org.example;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

/**
 * Unit test for simple App.
 */
public class ErrorHandlingTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void simple() {
        Flux<String> flux = Flux.just("A", "B", "C");

        StepVerifier.create(flux.log())
                .expectNext("A", "B", "C")
                .verifyComplete();
    }

    @Test
    public void onError() {
        Flux<String> flux = Flux.just("A", "B", "C")
                .concatWith(Flux.error(new Exception("There is an error")));

        StepVerifier.create(flux.log())
                .expectNext("A", "B", "C")
                .expectErrorMessage("There is an error")
                .verify();
    }

    @Test
    public void onErrorReturn() {
        Flux<String> flux = Flux.just("A", "B", "C")
                .concatWith(Flux.error(new Exception("This is an error")))
                .onErrorReturn("Error")
                .concatWith(Flux.just("D"));

        StepVerifier.create(flux.log())
                .expectNext("A", "B", "C")
                .expectNext("Error")
                .expectNext("D")
                .verifyComplete();
    }

    @Test
    public void onErrorResume() {
        Flux<String> flux = Flux.just("A", "B", "C")
                .concatWith(Flux.error(new Exception("This is an error")))
                .onErrorResume(t -> {
                    System.out.println("Error found: " + t.getMessage());
                    return Flux.just("Error");
                })
                .concatWith(Flux.just("D"));

        StepVerifier.create(flux.log())
                .expectNext("A", "B", "C")
                .expectNext("Error")
                .expectNext("D")
                .verifyComplete();
    }

    @Test
    public void onErrorMap() {
        Flux<String> flux = Flux.just("A", "B", "C")
                .concatWith(Flux.error(new Exception("This is an error")))
                .onErrorMap(t -> t.getMessage() == "This is an error" ? new Exception("Other exception") : t)
                .concatWith(Flux.just("D"));

        StepVerifier.create(flux.log())
                .expectNext("A", "B", "C")
                .expectErrorMessage("Other exception")
                .verify();
    }

    @Test
    public void doFinally() {
        Flux<String> flux = Flux.just("A", "B", "C", "D")
                .delayElements(Duration.ofMillis(501))
                .take(3) // cancel subscription
                .doFinally(signalType -> {
                    System.out.println("finally -> " + signalType);
                });

        StepVerifier.create(flux.log())
                .expectNext("A", "B", "C")
                .thenCancel()
                .verify();

    }
}
