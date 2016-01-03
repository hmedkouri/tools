package io.anaxo.net.ntlmproxy.http.message;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dogukan Sonmez
 */

public abstract class AbstractHttpResponse{

    private HttpRequest request;

    private HttpStatusCode statusCode;

    private String entity;

    private Map<String, String> headers;

    Map<String, String> extensionTypeMap = new HashMap<String, String>();

    public AbstractHttpResponse(HttpRequest request) {
        this.request = request;
        headers = new HashMap<String, String>();
        initializeExtensionMap();
    }

    private void initializeExtensionMap() {
        extensionTypeMap.put("", "content/unknown");
        extensionTypeMap.put("gif", "image/gif");
        extensionTypeMap.put("jpg", "image/jpeg\"");
        extensionTypeMap.put("jpeg", "text/html");
        extensionTypeMap.put("htm", "text/html");
        extensionTypeMap.put("html", "text/html");
        extensionTypeMap.put("txt", "text/plain");
        extensionTypeMap.put("java", "text/plain");
    }

    public String getContentType(String fileName) {
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }
        extension = extensionTypeMap.get(extension);
        if (extensionTypeMap.containsKey(extension)) {
            return extensionTypeMap.get(extension);
        }
        return "text/html";
    }

    public void addHeader(String name, String value) {
        this.headers.put(name, value);
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(HttpStatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public HttpRequest getRequest() {
        return request;
    }

}
