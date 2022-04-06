package com.example.consulclientconsumer.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@RequestMapping("/")
@RestController
class TalkController(
    @Autowired
    val webClientBuilder: WebClient.Builder
) {

    @GetMapping("/talk")
    fun talk(): Mono<Map<String, String>> {
        return webClientBuilder.build().get()
            .uri("/hello")
            .retrieve()
            .bodyToMono()
    }
}