package io.anaxo.net.ntlmproxy.http.exceptions;

/**
 * @author Dogukan Sonmez
 */

public class UnSupportedHttpMethodException extends RuntimeException{
    
    public UnSupportedHttpMethodException(String message){
        super(message);
    }
}
