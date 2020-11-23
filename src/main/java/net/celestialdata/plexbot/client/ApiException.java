/*
 * Plexbot
 * Provides all the API functions necessary for the Plexbot to function.
 *
 * OpenAPI spec version: 1.0.0
 * Contact: brandan.schmitz@celestialdata.net
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package net.celestialdata.plexbot.client;

import javax.annotation.processing.Generated;
import java.util.List;
import java.util.Map;

@Generated(value = "net.celestialdata.plexbot.codegen.v3.generators.java.JavaClientCodegen", date = "2020-11-23T04:57:45.670Z[GMT]")
public class ApiException extends Exception {
    private int code = 0;
    private Map<String, List<String>> responseHeaders = null;
    private String responseBody = null;

    @SuppressWarnings("unused")
    public ApiException() {
    }

    public ApiException(Throwable throwable) {
        super(throwable);
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable throwable, int code, Map<String, List<String>> responseHeaders, String responseBody) {
        super(message, throwable);
        this.code = code;
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
    }

    public ApiException(String message, int code, Map<String, List<String>> responseHeaders, String responseBody) {
        this(message, (Throwable) null, code, responseHeaders, responseBody);
    }

    public ApiException(String message, Throwable throwable, int code, Map<String, List<String>> responseHeaders) {
        this(message, throwable, code, responseHeaders, null);
    }

    @SuppressWarnings("unused")
    public ApiException(int code, Map<String, List<String>> responseHeaders, String responseBody) {
        this((String) null, (Throwable) null, code, responseHeaders, responseBody);
    }

    public ApiException(int code, String message) {
        super(message);
        this.code = code;
    }

    @SuppressWarnings("unused")
    public ApiException(int code, String message, Map<String, List<String>> responseHeaders, String responseBody) {
        this(code, message);
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
    }

    /**
     * Get the HTTP status code.
     *
     * @return HTTP status code
     */
    public int getCode() {
        return code;
    }

    /**
     * Get the HTTP response headers.
     *
     * @return A map of list of string
     */
    @SuppressWarnings("unused")
    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * Get the HTTP response body.
     *
     * @return Response body in the form of string
     */
    @SuppressWarnings("unused")
    public String getResponseBody() {
        return responseBody;
    }
}
