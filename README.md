# consul-client-consumer

Resilience4j



Core:
1. 滑动窗口：计数 or 计时
2. 失败率 & 慢调用率
3. 仓壁 Bulkhead：信号量 or 线程池
4. 限速器RateLimiter
5. 限时器TimeLimiter
6. 重试Retry
7. 缓存

支持Kotlin & OpenFeign
dependencies { 
    compile "io.github.resilience4j:resilience4j-kotlin:${resilience4jVersion}"            
    compile "io.github.resilience4j:resilience4j-retry:${resilience4jVersion}"
}
    
Spring Cloud
implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j")

WebClient & Customizer Circuit Breaker
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

CircuitBreaker Run (Kotlin & Reactive)
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

application.yml
resilience4j.circuitbreaker:
  instances:
    consul-client-provider:
      # 失败率
      failureRateThreshold: 50
      # 统计个数
      minimumNumberOfCalls: 10
      # 滑动窗口类型
      slidingWindowType: TIME_BASED
      # 滑动窗口大小
      slidingWindowSize: 10
      # 开启状态等待时间
      waitDurationInOpenState: 50s
      # 半开状态等待时间
      permittedNumberOfCallsInHalfOpenState: 3
Config property
Default Value
Description
failureRateThreshold 
50 
Configures the failure rate threshold in percentage.



When the failure rate is equal or greater than the threshold the CircuitBreaker transitions to open and starts short-circuiting calls. 
slowCallRateThreshold 
100 
Configures a threshold in percentage. The CircuitBreaker considers a call as slow when the call duration is greater than slowCallDurationThreshold



When the percentage of slow calls is equal or greater the threshold, the CircuitBreaker transitions to open and starts short-circuiting calls. 
slowCallDurationThreshold 
60000 [ms] 
Configures the duration threshold above which calls are considered as slow and increase the rate of slow calls. 
permittedNumberOfCalls

InHalfOpenState 
10 
Configures the number of permitted calls when the CircuitBreaker is half open. 
maxWaitDurationInHalfOpenState 
0 [ms] 
Configures a maximum wait duration which controls the longest amount of time a CircuitBreaker could stay in Half Open state, before it switches to open.

Value 0 means Circuit Breaker would wait infinitely in HalfOpen State until all permitted calls have been completed. 
slidingWindowType 
COUNT_BASED 
Configures the type of the sliding window which is used to record the outcome of calls when the CircuitBreaker is closed.

Sliding window can either be count-based or time-based.



If the sliding window is COUNT_BASED, the last slidingWindowSize calls are recorded and aggregated.

If the sliding window is TIME_BASED, the calls of the last slidingWindowSize seconds recorded and aggregated. 
slidingWindowSize 
100 
Configures the size of the sliding window which is used to record the outcome of calls when the CircuitBreaker is closed. 
minimumNumberOfCalls 
100 
Configures the minimum number of calls which are required (per sliding window period) before the CircuitBreaker can calculate the error rate or slow call rate.

For example, if minimumNumberOfCalls is 10, then at least 10 calls must be recorded, before the failure rate can be calculated.

If only 9 calls have been recorded the CircuitBreaker will not transition to open even if all 9 calls have failed. 
waitDurationInOpenState 
60000 [ms] 
The time that the CircuitBreaker should wait before transitioning from open to half-open. 
automaticTransition

FromOpenToHalfOpenEnabled 
false 
If set to true it means that the CircuitBreaker will automatically transition from open to half-open state and no call is needed to trigger the transition. A thread is created to monitor all the instances of CircuitBreakers to transition them to HALF_OPEN once waitDurationInOpenState passes. Whereas, if set to false the transition to HALF_OPEN only happens if a call is made, even after waitDurationInOpenState is passed. The advantage here is no thread monitors the state of all CircuitBreakers. 
recordExceptions 
empty 
A list of exceptions that are recorded as a failure and thus increase the failure rate.

Any exception matching or inheriting from one of the list counts as a failure, unless explicitly ignored via ignoreExceptions.

If you specify a list of exceptions, all other exceptions count as a success, unless they are explicitly ignored by ignoreExceptions. 
ignoreExceptions 
empty 
A list of exceptions that are ignored and neither count as a failure nor success.

Any exception matching or inheriting from one of the list will not count as a failure nor success, even if the exceptions is part of recordExceptions. 
recordFailurePredicate 
throwable -> true



By default all exceptions are recored as failures. 
A custom Predicate which evaluates if an exception should be recorded as a failure.

The Predicate must return true if the exception should count as a failure. The Predicate must return false, if the exception

should count as a success, unless the exception is explicitly ignored by ignoreExceptions. 
ignoreExceptionPredicate 
throwable -> false



By default no exception is ignored. 
A custom Predicate which evaluates if an exception should be ignored and neither count as a failure nor success.

The Predicate must return true if the exception should be ignored.

The Predicate must return false, if the exception should count as a failure. 

Sentinel

https://github.com/alibaba/Sentinel/wiki/%E4%BB%8B%E7%BB%8D



