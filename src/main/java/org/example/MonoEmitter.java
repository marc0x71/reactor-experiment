package org.example;

import lombok.Getter;
import reactor.core.publisher.Mono;

public class MonoEmitter {
    @Getter
    private final Mono<Long> mono;

    public MonoEmitter() {
        this.mono = Mono.create(callback -> {
            callback.success(System.currentTimeMillis() / 1000);
        });
    }


}
