package com.example.consulclientconsumer.client

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.timelimiter.TimeLimiterConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder
import org.springframework.cloud.client.circuitbreaker.Customizer
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration


@Configuration
class HelloWorldClient {
    @Value("\${services.provider}")
    lateinit var provider: String

    @LoadBalanced
    @Bean
    fun webClientBuilder(): WebClient.Builder {
        return WebClient.builder().baseUrl("http://$provider")
    }

    @Bean
    fun slowCustomizer(): Customizer<ReactiveResilience4JCircuitBreakerFactory> {
        return Customizer {
            it.configureDefault {
                Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration()
                    .apply {
                        id = "slow"
                        circuitBreakerConfig = CircuitBreakerConfig.ofDefaults()
                        timeLimiterConfig = TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(2)).build()
                    }
            }
        }
    }
}