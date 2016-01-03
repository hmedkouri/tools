package io.anaxo.http.ntlmproxy.scheduler;

import io.anaxo.http.ntlmproxy.processor.ClientRequestProcessor;

public interface RequestScheduler {

    public void schedule(ClientRequestProcessor clientRequestProcessor);

    public void shutDown();

}
