package io.anaxo.net.ntlmproxy.http.exceptions;

public class UnSupportedHttpMethodException extends RuntimeException{
    
    public UnSupportedHttpMethodException(String message){
        super(message);
    }
}
