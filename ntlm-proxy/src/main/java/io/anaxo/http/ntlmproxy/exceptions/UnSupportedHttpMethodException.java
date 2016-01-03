package io.anaxo.http.ntlmproxy.exceptions;

public class UnSupportedHttpMethodException extends RuntimeException{
    
    public UnSupportedHttpMethodException(String message){
        super(message);
    }
}
