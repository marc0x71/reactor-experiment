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
        scheduler.dispose();

        System.out.println("-----");
        // create
        Mono number = Mono.create(callback -> {
            callback.success(321);
        });
        number.subscribe(
                System.out::println,
                error -> System.err.println("error: " + error),
                () -> System.out.println("Mono consumed.")
        );

        // mono creation
        System.out.println("----- mono creation");
        number = Mono.create(callback -> {
            callback.error(new Exception("There is an error"));
        });
        number.subscribe(
                System.out::println,
                error -> System.out.println("error: " + error),
                () -> System.out.println("Mono consumed.")
        );

        System.out.println("-----");
        number = Mono.create(callback -> {
            callback.success();
        });
        number.subscribe(
                System.out::println,
                error -> System.err.println("error: " + error),
                () -> System.out.println("Mono consumed.")
        );

        System.out.println("----- mono emitter");
        MonoEmitter emitter = new MonoEmitter();
        emitter.getMono().subscribe(
                System.out::println,
                error -> System.err.println("error: " + error),
                () -> System.out.println("Mono consumed.")
        );

        FluxEmitter fluxEmitter = new FluxEmitter();
        fluxEmitter.attach().subscribe(
                System.out::println,
                error -> System.err.println("error: " + error),
                () -> System.out.println("fluxEmitter consumed.")
        );

        fluxEmitter.send(111);
        fluxEmitter.send(112);
        fluxEmitter.send(113);
        fluxEmitter.send(114);
        fluxEmitter.done();
        fluxEmitter.send(115);

        Thread.sleep(1500);
    }

    private static Integer showNumberAdReturn(String scenario, Integer v) {
        System.out.println(scenario + " - both " + v + " thread:" + Thread.currentThread().getName());
        return v;
    }

    private static void showNumber(String scenario, Double v) {
        System.out.println(scenario + " - both " + v + " thread:" + Thread.currentThread().getName());
    }
}
