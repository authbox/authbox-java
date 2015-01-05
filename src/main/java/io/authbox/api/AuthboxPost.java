package io.authbox.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import io.authbox.api.AuthboxVerdict;
import io.authbox.api.AuthboxVerdictRecipient;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;

class AuthboxPost implements Callable<AuthboxVerdict> {
    private JsonElement postData;
    private URL url;
    private AuthboxVerdictRecipient recipient;

    public AuthboxPost(URL url, JsonElement postData, AuthboxVerdictRecipient recipient) {
        this.postData = postData;
        this.url = url;
        this.recipient = recipient;
    }

    @Override
    public AuthboxVerdict call() throws Exception {
        try {
            String urlParameters = postData.toString();
            URLConnection conn = url.openConnection();
            conn.addRequestProperty("Content-type", "application/json");

            conn.setDoOutput(true);

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

            writer.write(urlParameters);
            writer.flush();

            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            JsonParser parser = new JsonParser();
            JsonObject response = parser.parse(reader).getAsJsonObject();
            writer.close();
            reader.close();

            AuthboxVerdict verdict = new AuthboxVerdict(response.getAsJsonPrimitive("type").getAsString(), response.get("info"));

            if (recipient != null) {
                recipient.receiveVerdict(verdict);
            }

            return verdict;
        } catch (Exception e) {
            e.printStackTrace();
            return new AuthboxVerdict("ALLOW", new JsonPrimitive(e.toString()));
        }
    }
}
