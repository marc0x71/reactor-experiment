package org.example;

import reactor.core.publisher.Flux;

public class FluxEmitter {
    private EmitterListener listener;

    public Flux<Integer> attach() {
        return Flux.create(emitter -> {
            FluxEmitter.this.listener = new EmitterListener() {
                @Override
                public void emit(Integer value) {
                    emitter.next(value);
                }

                @Override
                public void completed() {
                    emitter.complete();
                }
            };
            emitter.onDispose(() -> FluxEmitter.this.listener = null);
        });
    }

    public void send(Integer value) {
        if (listener != null) listener.emit(value);
    }

    public void done() {
        if (listener != null) listener.completed();
    }

    private interface EmitterListener {
        void emit(Integer value);

        void completed();
    }
}
