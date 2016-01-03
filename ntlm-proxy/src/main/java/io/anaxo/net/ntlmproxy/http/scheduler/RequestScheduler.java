package io.anaxo.net.ntlmproxy.http.scheduler;

import io.anaxo.net.ntlmproxy.http.processor.ClientRequestProcessor;

public interface RequestScheduler {

    public void schedule(ClientRequestProcessor clientRequestProcessor);

    public void shutDown();

}
