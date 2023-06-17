package ru.bmstu.oberoncompiler.antlr4.config;

public enum Attributes {
    ELEMENT_TYPE("element_type")
    ;

    private final String text;

    Attributes(final String data) {
        text = data;
    }

    public String toString() {
        return text;
    }
}
