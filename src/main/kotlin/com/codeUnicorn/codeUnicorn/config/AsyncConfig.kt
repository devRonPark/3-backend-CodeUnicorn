package com.codeUnicorn.codeUnicorn.config

import java.util.concurrent.Executor
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurerSupport
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
@EnableAsync
class AsyncConfig : AsyncConfigurerSupport() {
    // 비동기로 호출하는 Thread에 대한 설정
    override fun getAsyncExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 2 // 기본적으로 실행 대기 중인 Thread 갯수
        executor.maxPoolSize = 10 // 동시 동작하는, 최대 Thread 갯수
        executor.setQueueCapacity(500)
        executor.setThreadNamePrefix("codeunicorn-async-") // spring이 생성하는 쓰레드의 접두사 지정
        executor.initialize()
        return executor
    }
}
