package io.authbox.api;

import com.google.gson.JsonObject;
import javax.servlet.http.HttpServletRequest;

public interface AuthboxRequestDataProvider {
    public JsonObject getRequestData(HttpServletRequest request);
}
