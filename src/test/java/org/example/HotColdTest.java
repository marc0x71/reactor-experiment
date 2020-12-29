package org.example;

import org.junit.Test;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;

import java.time.Duration;

public class HotColdTest {

    @Test
    public void coldPublisher() throws InterruptedException {
        Flux<Integer> flux = Flux.range(1, 5).delayElements(Duration.ofSeconds(1));

        flux.subscribe(x -> System.out.println("sub1 " + x));
        Thread.sleep(2000);

        flux.subscribe(x -> System.out.println("sub2 " + x));
        Thread.sleep(4000);
    }

    @Test
    public void hotPublisher() throws InterruptedException {
        Flux<Integer> flux = Flux.range(1, 5).delayElements(Duration.ofSeconds(1));

        ConnectableFlux<Integer> connectableFlux = flux.publish();
        connectableFlux.connect();

        connectableFlux.subscribe(x -> System.out.println("sub1 " + x));
        Thread.sleep(2000);

        connectableFlux.subscribe(x -> System.out.println("sub2 " + x));
        Thread.sleep(4000);
    }

}
