package com.wolf.shoot.service.rpc.server;

import com.snowcattle.game.thread.policy.*;
import com.wolf.shoot.common.ThreadNameFactory;
import com.wolf.shoot.common.annotation.BlockingQueueType;
import com.wolf.shoot.common.constant.GlobalConstants;
import com.wolf.shoot.common.constant.Loggers;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

/**
 * Created by jwp on 2017/3/8.
 */
@Service
public class RpcHandlerThreadPool {

    private final Logger logger = Loggers.threadLogger;
    private ExecutorService executor;

    private RejectedExecutionHandler createPolicy() {
        RejectedPolicyType rejectedPolicyType = RejectedPolicyType.fromString(System.getProperty(RpcSystemConfig.SystemPropertyThreadPoolRejectedPolicyAttr, "CallerRunsPolicy"));

        switch (rejectedPolicyType) {
            case BLOCKING_POLICY:
                return new BlockingPolicy();
            case CALLER_RUNS_POLICY:
                return new CallerRunsPolicy();
            case ABORT_POLICY:
                return new AbortPolicy();
            case REJECTED_POLICY:
                return new RejectedPolicy();
            case DISCARDED_POLICY:
                return new DiscardedPolicy();
        }

        return null;
    }

    private BlockingQueue<Runnable> createBlockingQueue(int queues) {
        BlockingQueueType queueType = BlockingQueueType.fromString(System.getProperty(RpcSystemConfig.SystemPropertyThreadPoolQueueNameAttr, "LinkedBlockingQueue"));

        switch (queueType) {
            case LINKED_BLOCKING_QUEUE:
                return new LinkedBlockingQueue<Runnable>();
            case ARRAY_BLOCKING_QUEUE:
                return new ArrayBlockingQueue<Runnable>(RpcSystemConfig.PARALLEL * queues);
            case SYNCHRONOUS_QUEUE:
                return new SynchronousQueue<Runnable>();
        }

        return null;
    }

    public Executor createExecutor(int threads, int queues) {
        String name = GlobalConstants.Thread.RPC_HANDLER;
        ThreadPoolExecutor executor = new ThreadPoolExecutor(threads, threads, 0, TimeUnit.MILLISECONDS, createBlockingQueue(queues), new ThreadNameFactory(name, false), createPolicy());
        this.executor = executor;
        return executor;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }
}

