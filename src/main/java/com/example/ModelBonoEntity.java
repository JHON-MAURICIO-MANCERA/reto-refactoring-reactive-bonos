package com.example;

public class ModelBonoEntity {
    private final String code;
    private final String date;

    public ModelBonoEntity(String code, String date) {
        this.code = code;
        this.date = date;
    }

    public String getCode() {
        return code;
    }

    public String getDate() {
        return date;
    }
}