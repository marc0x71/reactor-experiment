package org.example;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class FluxEmitter<T> {
    private final Sinks.Many<T> replaySink;

    public FluxEmitter() {
        replaySink = Sinks.many().replay().all();
    }

    public Flux<T> attach() {
        return replaySink.asFlux();
    }

    public void next(T value) {
        replaySink.tryEmitNext(value);
    }

    public void error(Throwable t) {
        replaySink.tryEmitError(t);
    }

    public void complete() {
        replaySink.tryEmitComplete();
    }

}
