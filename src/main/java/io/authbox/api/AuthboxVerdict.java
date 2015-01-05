package io.authbox.api;

import com.google.gson.JsonElement;

public class AuthboxVerdict {
    private String type;
    private JsonElement info;

    public AuthboxVerdict(String type, JsonElement info) {
        this.type = type;
        this.info = info;
    }

    public String getType() {
        return type;
    }

    public JsonElement getInfo() {
        return info;
    }
}
