package ru.bmstu.oberoncompiler.antlr4.exception;

public class WrongModuleNameException extends RuntimeException {
    public static String msg = "Module's name at the beginning (%s) is different from name at the end (%s).";

    public WrongModuleNameException(String startName, String endName) {
        super(String.format(msg, startName, endName));
    }
}
