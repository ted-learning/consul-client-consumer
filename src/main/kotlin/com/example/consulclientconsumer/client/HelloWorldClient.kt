package com.example.consulclientconsumer.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@LoadBalancerClient(name = "say-hello")
@Configuration
class HelloWorldClient {
    @Value("\${services.provider}")
    lateinit var provider: String

    @LoadBalanced
    @Bean
    fun webClientBuilder(): WebClient.Builder {
        return WebClient.builder().baseUrl("http://$provider")
    }
}