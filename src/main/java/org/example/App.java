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
/*
        simpleMono();

        simpleFlux();

        usingSchedulers();

        monoCreate();

        monoEmitter();

        fluxEmitter();
*/

        errorHandling();

    }

    private static void errorHandling() {

        Flux.just(12, 44, 11, 0, 66, 22)
                .map(i -> 100 / i)
                .doOnError(e -> System.out.println("doOnError: " + e.getMessage()))
                .onErrorResume(e -> {
                    System.out.println("onErrorResume: " + e.getMessage());
                    return Flux.just(-1);
                })
                .subscribe(
                        System.out::println,
                        error -> System.out.println("error: " + error.getMessage())
                );

        System.out.println("-----");

        Flux.just(12, 44, 11, 0, 66, 22)
                .map(i -> {
                    try {
                        return 100 / i;
                    } catch (Exception e) {
                        return Flux.empty();
                    }
                })
                .filter(x -> x != Flux.empty())
                .doOnError(t -> System.out.println("error: " + t.getMessage()))
                .onErrorReturn(-1) // error handling example
                .subscribe(x -> System.out.println("> " + x.toString()));

        System.out.println("-----");

        Flux.just(12, 44, 11, 0, 66, 22)
                .map(i -> 100 / i)
                .doOnError(t -> System.out.println("error: " + t.getMessage()))
                .onErrorReturn(-1) // error handling example
                .subscribe(x -> System.out.println("> " + x.toString()));

    }

    private static Flux<Integer> call(Integer i) {
        return Flux.just(100 / i);
    }

    private static void fluxEmitter() throws InterruptedException {
        System.out.println("----- flux emitter");
        FluxEmitter<Integer> fluxEmitter = new FluxEmitter<>();

        fluxEmitter.attach()
                .doOnError(e -> System.out.println("error1:" + e.getMessage()))
                .onErrorResume(Exception.class, __ -> Flux.empty())
                .doOnNext(i -> System.out.println("1. " + i))
                .doOnComplete(() -> System.out.println("1. completed"))
                .subscribe();

        fluxEmitter.attach()
                //.filter(x -> x % 2 == 0)
                .doOnError(e -> System.out.println("error2a:" + e.getMessage()))
                .onErrorResume(Exception.class, __ -> Flux.empty())
                .subscribe(
                        i -> System.out.println("2. " + i),
                        error -> System.out.println("error2b: " + error.getMessage()),
                        () -> System.out.println("2. completed"));

        fluxEmitter.next(111);
        fluxEmitter.next(112);
        fluxEmitter.next(113);
        fluxEmitter.error(new Exception("This is an error"));
        fluxEmitter.next(114);
        fluxEmitter.next(115);
        fluxEmitter.complete();

        Thread.sleep(1500);
        System.out.println("The End");
    }

    private static void monoEmitter() {
        System.out.println("----- mono emitter");
        MonoEmitter emitter = new MonoEmitter();
        emitter.getMono().subscribe(
                System.out::println,
                error -> System.err.println("error: " + error),
                () -> System.out.println("Mono consumed.")
        );
    }

    private static void monoCreate() {
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
    }

    private static void usingSchedulers() throws InterruptedException {
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
    }

    private static void simpleFlux() {
        // flux
        Flux<Integer> squared = Flux.range(1, 5).map(x -> x * x);

        // map
        squared.map(x -> x + " ").subscribe(System.out::print);
        System.out.println();

        // convert to a mono of integer list
        squared.collectList().subscribe(System.out::println);
    }

    private static void simpleMono() {
        // mono
        Mono<Integer> mono = Mono.just(123);

        // show on System.out
        mono.subscribe(System.out::println);

        // block and get value
        System.out.println("first call: " + mono.block());
        System.out.println("second block call: " + mono.block(Duration.of(1000, ChronoUnit.MILLIS)));
    }

    private static Integer showNumberAdReturn(String scenario, Integer v) {
        System.out.println(scenario + " - both " + v + " thread:" + Thread.currentThread().getName());
        return v;
    }

    private static void showNumber(String scenario, Double v) {
        System.out.println(scenario + " - both " + v + " thread:" + Thread.currentThread().getName());
    }
}
