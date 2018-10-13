package com.retarcorp.rchatapp.Utils;

public class Response {
    public static String getExceptionJSON(String message) {
        return "{\"status\":\"ERROR\",\"message\":\"" + message + "\"}";
    }
}
