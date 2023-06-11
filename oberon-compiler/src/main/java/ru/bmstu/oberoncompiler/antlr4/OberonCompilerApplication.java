package ru.bmstu.oberoncompiler.antlr4;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import ru.bmstu.oberoncompiler.antlr4.service.OberonCompilerService;


@Slf4j
@SpringBootApplication
public class OberonCompilerApplication {
    @Autowired
    private OberonCompilerService service;

    public static void main(String[] args) {
        SpringApplication.run(OberonCompilerApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void run() {
        String filename = "./src/test/data/test_1.txt";

        try {
            service.process(filename);
        } catch (Exception e) {
            log.error("ERROR: " + e.getMessage());
        }
    }
}
