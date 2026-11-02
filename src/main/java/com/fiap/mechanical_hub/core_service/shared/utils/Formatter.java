package com.fiap.mechanical_hub.core_service.shared.utils;

public class Formatter {

    private Formatter() {}

    public static String removeFormatting(String document) {
        if (document == null) { return null; }
        return document.replaceAll("\\D", "");
    }

}
