package ru.bmstu.oberoncompiler.antlr4.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppParams {
    @Value("${app.buildDir}")
    public String buildDir;

    @Value("${app.module.name}")
    public String moduleName;
}
