package io.anaxo.net.ntlmproxy.http.message;

import java.util.Map;

/**
 * @author Dogukan Sonmez
 */

public interface HttpResponse {

    public HttpStatusCode getStatusCode();
    
    public Map<String,String> getHeaders();
    
    public String getEntity();


}
