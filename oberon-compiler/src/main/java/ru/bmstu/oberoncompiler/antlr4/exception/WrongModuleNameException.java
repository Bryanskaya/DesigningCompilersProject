package ru.bmstu.oberoncompiler.antlr4.exception;

public class WrongModuleNameException extends RuntimeException {
    public static String msg = "Module's name at the beginning is different from name at the end.";

    public WrongModuleNameException() {
        super(msg);
    }
}
