package io.anaxo.http.ntlmproxy.scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.anaxo.http.ntlmproxy.processor.ClientRequestProcessor;

public class ClientRequestScheduler implements RequestScheduler {

    ExecutorService executor;

    public ClientRequestScheduler(int availableThreads) {
        executor = Executors.newFixedThreadPool(availableThreads);
    }

    public void schedule(final ClientRequestProcessor processor) {
        Runnable runnable = new Runnable() {
            public void run() {
                processor.process();
            }
        };
        executor.execute(runnable);
    }

    public void shutDown(){
        executor.shutdown();
    }
}
