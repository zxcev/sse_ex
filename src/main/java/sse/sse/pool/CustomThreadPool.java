package sse.sse.pool;

import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class CustomThreadPool {

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            // corePoolSize: 기본적으로 유지할 최소 스레드 수
            2,
            // maximumPoolSize: 최대 스레드 수
            4,
            // keepAliveTime: 초과 스레드가 유지되는 시간
            60,
            // keepAliveTime의 단위
            TimeUnit.SECONDS,
            // 작업 대기열 (BlockingQueue)
            new ArrayBlockingQueue<>(10)
            // 스레드 팩토리 new CustomRejectedExecutionHandler() // 작업 거부 정책
//            new CustomThreadFactory()
    );

    public void run(final Runnable fn) {
        executor.getThreadFactory().newThread(fn).start();
    }
}
