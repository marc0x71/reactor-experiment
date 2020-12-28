package org.example;

import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

public class FluxEmitter<T> {
    private final List<EmitterListener<T>> listeners = new ArrayList<>();
    private final Flux<T> flux;

    public FluxEmitter() {
        this.flux = Flux.create(emitter -> {
            EmitterListener<T> listener = FluxEmitter.this.attachListemer(new EmitterListener<T>() {
                @Override
                public void next(T value) {
                    emitter.next(value);
                }

                @Override
                public void error(Throwable t) {
                    emitter.error(t);
                }

                @Override
                public void complete() {
                    emitter.complete();
                }
            });
            emitter.onDispose(() -> FluxEmitter.this.detachListener(listener));
        });
    }

    private void detachListener(EmitterListener<T> listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    private EmitterListener<T> attachListemer(EmitterListener<T> listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
        return listener;
    }

    public Flux<T> attach() {
        return flux;
    }

    public void next(T value) {
        synchronized (listeners) {
            for (EmitterListener<T> listener : listeners) {
                listener.next(value);
            }
        }
    }

    public void error(Throwable t) {
        synchronized (listeners) {
            for (int i = 0; i < listeners.size(); i++) {
                EmitterListener<T> listener = listeners.get(0);
                listener.error(t);
                i = 0;
            }
        }
    }

    public void complete() {
        synchronized (listeners) {
            for (int i = 0; i < listeners.size(); i++) {
                EmitterListener<T> listener = listeners.get(0);
                listener.complete();
                i = 0;
            }
        }
    }

    private interface EmitterListener<T> {
        void next(T value);

        void error(Throwable t);

        void complete();
    }
}
