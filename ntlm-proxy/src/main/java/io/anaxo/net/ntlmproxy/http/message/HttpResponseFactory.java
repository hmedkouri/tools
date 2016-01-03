package io.anaxo.net.ntlmproxy.http.message;

import io.anaxo.net.ntlmproxy.http.exceptions.UnSupportedHttpMethodException;

/**
 * @author Dogukan Sonmez
 */

public class HttpResponseFactory {

    public static HttpResponse createHttpResponse(HttpRequest request) throws UnSupportedHttpMethodException {

        switch (request.getMethod()) {
            case GET:
                return new HttpGetResponse(request);
            case POST:
                return new HttpPostResponse(request);
        }

        throw new UnSupportedHttpMethodException("Un supported HTTP request method :" + request.getMethod().toString());

    }
}
