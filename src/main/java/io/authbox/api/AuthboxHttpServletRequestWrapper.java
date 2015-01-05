package io.authbox.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.authbox.api.AuthboxConfig;
import io.authbox.api.AuthboxPost;
import io.authbox.api.AuthboxRequestDataProvider;
import io.authbox.api.AuthboxVerdict;
import io.authbox.api.AuthboxVerdictRecipient;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class AuthboxHttpServletRequestWrapper extends HttpServletRequestWrapper implements AuthboxVerdictRecipient {
    private static final int NUM_THREADS = 5;
    private static ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

    private String localMachineID;
    private boolean isNewLocalMachineID;
    private boolean didCheck = false;
    private boolean didLog = false;
    private String checkVerdictType;
    private JsonObject checkData;
    private Future<AuthboxVerdict> checkFuture;

    public AuthboxHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);

        localMachineID = getLocalMachineIDFromCookie();
        isNewLocalMachineID = localMachineID == null;
        if (localMachineID == null) {
            localMachineID = UUID.randomUUID().toString();
        }
    }

    public void receiveVerdict(AuthboxVerdict verdict) {
        checkVerdictType = verdict.getType();
    }

    private String getAuthboxCookieName(String cookieType) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((AuthboxConfig.apiKey + ":" + cookieType).getBytes("UTF-8"));
            return new BigInteger(1, hash).toString(16);
        } catch (Exception e) {
            // Unlikely to ever happen
            return "__authboxLMID";
        }
    }

    private String getLocalMachineIDFromCookie() {
        String authboxCookieName = getAuthboxCookieName("localMachineID");
        Cookie[] cookies = getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie: cookies) {
            if (cookie.getName().equals(authboxCookieName)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public Future<AuthboxVerdict> check(JsonObject data) throws MalformedURLException {
        if (didCheck) {
            // TODO: warn
            return checkFuture;
        }
        didCheck = true;
        checkData = data;
        checkFuture = fire(true, data);
        return checkFuture;
    }

    public void log(JsonObject data) throws MalformedURLException {
        if (didCheck && !checkVerdictType.equals("ALLOW")) {
            return;
        }

        if (didLog) {
            throw new RuntimeException("Already called log() for this request");
        }
        didLog = true;

        fire(false, data);
    }

    public void logDefaultAction() throws MalformedURLException {
        if (didLog) {
            return;
        }
        log(checkData == null ? new JsonObject() : checkData);
    }

    private Future<AuthboxVerdict> fire(boolean isCheck, JsonObject userData) throws MalformedURLException {
        JsonObject data = new JsonObject();

        // Merge in request data
        AuthboxRequestDataProvider authboxRequestDataProvider = AuthboxConfig.authboxRequestDataProvider;

        if (authboxRequestDataProvider != null) {
            for (Map.Entry<String, JsonElement>entry: authboxRequestDataProvider.getRequestData(this).entrySet()) {
                data.add(entry.getKey(), entry.getValue());
            }
        }

        for (Map.Entry<String, JsonElement>entry: userData.entrySet()) {
            data.add(entry.getKey(), entry.getValue());
        }

        data.addProperty("$timestamp", System.currentTimeMillis());
        data.addProperty("$apiKey", AuthboxConfig.apiKey);
        data.addProperty("$secretKey", AuthboxConfig.secretKey);

        String endpointURL = getRequestURL().toString();
        if (getQueryString() != null) {
            endpointURL += "?" + getQueryString();
        }

        data.addProperty("$endpointURL", endpointURL);
        data.add("$ipAddress", getIP());
        data.addProperty("$userAgent", getHeader("user-agent"));
        data.addProperty("$referer", getHeader("referer"));
        data.addProperty("$host", getHeader("host"));

        if (localMachineID != null) {
            JsonObject localMachineIDJson = new JsonObject();
            localMachineIDJson.addProperty("$key", localMachineID);
            localMachineIDJson.addProperty("$new", isNewLocalMachineID);
            data.add("$localMachineID", localMachineIDJson);
        }

        return executor.submit(new AuthboxPost(new URL(AuthboxConfig.endpoint + (isCheck ? "/action_check" : "/action_log")), data, isCheck ? this : null));
    }

    private JsonObject getIP() {
        JsonObject rv = new JsonObject();
        rv.addProperty("$clientIP", getRemoteAddr());
        rv.addProperty("$httpCfConnectingIP", getHeader("cf-connecting-ip"));
        rv.addProperty("$httpClientIP", getHeader("client-ip"));
        rv.addProperty("$httpXClientIP", getHeader("x-client-ip"));
        rv.addProperty("$httpForwardedFor", getHeader("forwarded-for"));
        rv.addProperty("$httpXForwardedFor", getHeader("x-forwarded-for"));
        rv.addProperty("$httpForwarded", getHeader("forwarded"));
        rv.addProperty("$httpXForwarded", getHeader("x-forwarded"));
        return rv;
    }

    public String getLocalMachineID() {
        return localMachineID;
    }

    public String getLocalMachineIDCookieName() {
        return getAuthboxCookieName("localMachineID");
    }

    public String getDidGetPixelCookieName() {
        return getAuthboxCookieName("didGetPixel");
    }
}
