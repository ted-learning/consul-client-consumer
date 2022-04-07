package com.example.consulclientconsumer.controller

import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@RequestMapping("/")
@RestController
class TalkController(
    private val webClientBuilder: WebClient.Builder,
    private val reactiveCircuitBreakerFactory: ReactiveCircuitBreakerFactory<*, *>
) {

    @GetMapping("/talk")
    fun talk(): Mono<Map<String, String>> {
        return reactiveCircuitBreakerFactory.create("consul-client-provider")
            .run(
                webClientBuilder.build().get()
                    .uri("/hello")
                    .retrieve()
                    .bodyToMono<Map<String, String>>()
            ) {
                Mono.just(mapOf("data" to "slow timeout"))
            }
    }
}