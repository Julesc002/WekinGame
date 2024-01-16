package com.wekinGame.ressources;

public enum HTTPCodes {

    OK("200"),
    BAD_REQUEST("400"),
    FORBIDDEN("403"),
    NOT_FOUND("404"),
    CONFLICT("409"),
    INTERNAL_SERVER_ERROR("500");

    private String httpCode;

    private HTTPCodes(String httpCode) {
        this.httpCode = httpCode;
    }

    public String getHttpCode() {
        return this.httpCode;
    }
}