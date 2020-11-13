package net.celestialdata.plexbot.apis;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Provides a method for authorizing against the CloudFlare DDoS protection
 * page that was added to the YTS API website
 */
public class CloudflareAuthorizer {
    private final HttpClient httpClient;
    private final HttpClientContext httpClientContext;
    private final Pattern jsChallenge = Pattern.compile("name=\"jschl_vc\" value=\"(.+?)\"");
    private final Pattern password = Pattern.compile("name=\"pass\" value=\"(.+?)\"");
    private final Pattern jsScript = Pattern.compile("setTimeout\\(function\\(\\)\\{\\s+(var s,t,o,p,b,r,e,a,k,i,n,g,f.+?\\r?\\n[\\s\\S]+?a\\.value =.+?)\\r?\\n");
    private final ScriptEngineManager engineManager = new ScriptEngineManager();
    private final ScriptEngine engine = engineManager.getEngineByName("graal.vm");
    private String clearanceCookie = "";

    public CloudflareAuthorizer(HttpClient httpClient, HttpClientContext httpClientContext) {
        this.httpClient = httpClient;
        this.httpClientContext = httpClientContext;
    }

    private String convertStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public boolean tryAuthorization(String url) throws IOException, ScriptException {
        URL cloudFlareUrl = new URL(url);

        try {
            int retries = 5;
            int timer = 4500;
            Response response = getResponse(url, url);

            while (response.httpStatus == HttpStatus.SC_SERVICE_UNAVAILABLE && retries-- > 0) {
                String answer = getJsAnswer(cloudFlareUrl, response.responseText);
                String jschl_vc = new PatternStreamer(jsChallenge).results(response.responseText).findFirst().orElse("");
                String pass = new PatternStreamer(password).results(response.responseText).findFirst().orElse("");

                String authUrl = String.format("https://%s/cdn-cgi/l/chk_jschl?jschl_vc=%s&pass=%s&jschl_answer=%s",
                        cloudFlareUrl.getHost(),
                        URLEncoder.encode(jschl_vc, StandardCharsets.UTF_8),
                        URLEncoder.encode(pass, StandardCharsets.UTF_8),
                        answer);

                Thread.sleep(timer);
                response = getResponse(authUrl, url);
            }

            if (response.httpStatus != HttpStatus.SC_OK) {
                if (response.httpStatus == HttpStatus.SC_FORBIDDEN && response.responseText.contains("cf-captcha-container")) {
                    Thread.sleep(15000);
                }
                return false;
            }

        } catch (InterruptedException ie) {
            return false;
        }

        Optional<Cookie> cfClearanceCookie = httpClientContext.getCookieStore().getCookies()
                .stream()
                .filter(cookie -> cookie.getName().equals("cf_clearance"))
                .findFirst();

        cfClearanceCookie.ifPresent(cookie -> clearanceCookie = cookie.getValue());

        return true;
    }

    public String getClearanceCookie() {
        return clearanceCookie;
    }

    private Response getResponse(String url, String referer) throws IOException {
        HttpGet getRequest = new HttpGet(url);

        if (referer != null)
            getRequest.setHeader(HttpHeaders.REFERER, referer);

        int hardTimeout = 30; // seconds
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                getRequest.abort();
            }
        };

        new Timer(true).schedule(task, hardTimeout * 1000);
        HttpResponse httpResponse = httpClient.execute(getRequest, httpClientContext);
        String responseText = convertStreamToString(httpResponse.getEntity().getContent());
        int httpStatus = httpResponse.getStatusLine().getStatusCode();
        task.cancel();
        httpResponse.getEntity().getContent().close();
        ((CloseableHttpResponse) httpResponse).close();
        return new Response(httpStatus, responseText);
    }

    private String getJsAnswer(URL url, String responseHtml) throws ScriptException {
        Matcher result = jsScript.matcher(responseHtml);

        if (result.find()) {
            String jsCode = result.group(1);
            jsCode = jsCode.replaceAll("a\\.value = (.+ \\+ t\\.length).+", "$1");
            jsCode = jsCode.replaceAll("\\s{3,}[a-z](?: = |\\.).+", "").replace("t.length", String.valueOf(url.getHost().length()));
            jsCode = jsCode.replaceAll("[\\n\\\\']", "");

            if (!jsCode.contains("toFixed")) {
                throw new IllegalStateException("BUG: could not find toFixed inside CF JS challenge code");
            }

            return new BigDecimal(engine.eval(jsCode).toString()).setScale(10, RoundingMode.HALF_UP).toString();
        }
        throw new IllegalStateException("BUG: could not find initial CF JS challenge code in: " + responseHtml);
    }

    private static class Response {
        private final int httpStatus;
        private final String responseText;

        Response(int httpStatus, String responseText) {
            this.httpStatus = httpStatus;
            this.responseText = responseText;
        }
    }

    private static final class PatternStreamer {
        private final Pattern pattern;

        public PatternStreamer(Pattern regex) {
            this.pattern = regex;
        }

        public Stream<String> results(CharSequence input) {
            List<String> list = new ArrayList<>();
            for (Matcher m = this.pattern.matcher(input); m.find(); )
                for (int idx = 1; idx <= m.groupCount(); ++idx) {
                    list.add(m.group(idx));
                }
            return list.stream();
        }
    }
}