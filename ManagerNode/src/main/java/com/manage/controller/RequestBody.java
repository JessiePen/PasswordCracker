package com.manage.controller;

public class RequestBody {
    private final String passwordMd5;

    public RequestBody(String passwordMd5) {
        this.passwordMd5 = passwordMd5;
    }

    public String getPasswordMd5() {
        return passwordMd5;
    }
}
