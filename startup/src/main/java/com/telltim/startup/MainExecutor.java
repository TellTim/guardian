package com.telltim.startup;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;


/**
 * @author Tim.WJ
 */
public class MainExecutor implements Executor {

    private final BlockingQueue<Runnable> blockingQueue;

    public MainExecutor() {
        blockingQueue = new LinkedBlockingDeque<>();
    }

    @Override
    public void execute(Runnable command) {
        blockingQueue.offer(command);
    }

    public Runnable take() throws InterruptedException {
        return blockingQueue.take();
    }
}
