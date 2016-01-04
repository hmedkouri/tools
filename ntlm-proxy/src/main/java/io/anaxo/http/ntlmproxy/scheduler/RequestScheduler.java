package io.anaxo.http.ntlmproxy.scheduler;

import io.anaxo.http.ntlmproxy.processor.ClientProcessor;

public interface RequestScheduler {

    public void schedule(ClientProcessor clientRequestProcessor);

    public void shutDown();

}
