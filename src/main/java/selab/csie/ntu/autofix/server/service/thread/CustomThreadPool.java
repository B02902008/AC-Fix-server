package selab.csie.ntu.autofix.server.service.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CustomThreadPool extends ThreadPoolExecutor {

    private Integer activeCount;

    public CustomThreadPool(
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            TimeUnit unit,
            BlockingQueue<Runnable> workQueue,
            RejectedExecutionHandler handler
    ) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
        this.activeCount = 0;
    }

    @Override
    public void afterExecute(Runnable r, Throwable t) {
        this.activeCount -= 1;
    }

    @Override
    public void beforeExecute(Thread t, Runnable r) {
        this.activeCount += 1;
    }

    @Override
    public int getActiveCount() {
        return this.activeCount;
    }

}
