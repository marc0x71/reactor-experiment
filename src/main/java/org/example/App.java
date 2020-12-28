package org.example;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * reactor-experiment
 */
public class App {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("reactor-experiment!");

        // mono
        Mono<Integer> mono = Mono.just(123);

        // show on System.out
        mono.subscribe(System.out::println);

        // block and get value
        System.out.println("first call: " + mono.block());
        System.out.println("second block call: " + mono.block(Duration.of(1000, ChronoUnit.MILLIS)));

        // flux
        Flux<Integer> squared = Flux.range(1, 5).map(x -> x * x);

        // map
        squared.map(x -> x + " ").subscribe(System.out::print);
        System.out.println();

        // convert to a mono of integer list
        squared.collectList().subscribe(System.out::println);

        System.out.println("-------- subscribeOn & publishOn");

        // subscribeOn & publishOn
        Scheduler scheduler = Schedulers.newParallel("p", 4);

        // A - emitter and subscriber in main thread
        Flux.range(1, 5).map(x -> x * x)
                .map(x -> showNumberAdReturn("A", x))
                .map(Math::sqrt)
                .subscribe(x -> showNumber("A", x));

        // B - emitter and subscriber in same thread
        Flux.range(1, 5).map(x -> x * x)
                .map(x -> showNumberAdReturn("B", x))
                .subscribeOn(scheduler)
                .map(Math::sqrt)
                .subscribe(x -> showNumber("B", x));

        // C - emitter on main thead and subscriber on parallel thread
        Flux.range(1, 5).map(x -> x * x)
                .map(x -> showNumberAdReturn("C", x))
                .publishOn(scheduler)
                .map(Math::sqrt)
                .subscribe(x -> showNumber("C", x));

        // D - emitter on main thread and subscriber in different threads
        Flux.range(1, 5).map(x -> x * x).subscribeOn(scheduler)
                .map(x -> showNumberAdReturn("D", x))
                .map(Math::sqrt)
                .publishOn(scheduler)
                .subscribe(x -> showNumber("D", x));

        Thread.sleep(1000);
        System.out.println("the end.");
        scheduler.dispose();
    }

    private static Integer showNumberAdReturn(String scenario, Integer v) {
        System.out.println(scenario + " - both " + v + " thread:" + Thread.currentThread().getName());
        return v;
    }

    private static void showNumber(String scenario, Double v) {
        System.out.println(scenario + " - both " + v + " thread:" + Thread.currentThread().getName());
    }
}
