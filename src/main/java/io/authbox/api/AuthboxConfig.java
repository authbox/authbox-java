package io.authbox.api;

import io.authbox.api.AuthboxRequestDataProvider;

class AuthboxConfig {
    public static String apiKey = System.getProperty("authbox.apiKey");
    public static String secretKey = System.getProperty("authbox.secretKey");
    public static String endpoint = System.getProperty("authbox.endpoint", "https://api.authbox.io/api");

    public static AuthboxRequestDataProvider authboxRequestDataProvider;

    static {
        String requestDataProviderClassName = System.getProperty("authbox.requestDataProvider");
        try {
            if (requestDataProviderClassName != null) {
                authboxRequestDataProvider = (AuthboxRequestDataProvider)Class.forName(requestDataProviderClassName).newInstance();
            }
        } catch (Exception e) {
            System.err.println("Exception while instantiating AuthboxRequestDataProvider named " + requestDataProviderClassName + ":");
            e.printStackTrace();
        }
    }
}
