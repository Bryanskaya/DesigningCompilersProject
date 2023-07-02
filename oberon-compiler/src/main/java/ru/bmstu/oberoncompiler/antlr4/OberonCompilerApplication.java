package ru.bmstu.oberoncompiler.antlr4;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Profiles;
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
    public void run(ApplicationReadyEvent event) {
        if (event.getApplicationContext().getEnvironment().acceptsProfiles(Profiles.of("test")))
            return;
//        String filename = "./src/test/data/test_assign_add_value_real.txt";
//        String filename = "./src/test/data/test_assign_add_value_int.txt";
//        String filename = "./src/test/data/test_several_vars.txt";
//        String filename = "./src/test/data/test_several_types.txt";
//        String filename = "./src/test/data/test_assign_minus_int.txt";
//        String filename = "./src/test/data/test_assign_minus_real.txt";
//        String filename = "./src/test/data/test_assign_subtract_value_int.txt";
//        String filename = "./src/test/data/test_assign_mul_value_int.txt";
//        String filename = "./src/test/data/test_assign_division_value_int.txt";
//        String filename = "./src/test/data/test_brackets.txt";
//        String filename = "./src/test/data/test_assign_array_int.txt";
//        String filename = "./src/test/data/test_if_else.txt";
//        String filename = "./src/test/data/test_if_else_inner.txt";
//        String filename = "./src/test/data/test_elseif.txt";
//        String filename = "./src/test/data/test_while.txt";
//        String filename = "./src/test/data/test_fib.txt";
        String filename = "./src/test/data/test_fib_array.txt";
//        String filename = "./src/test/data/test_elsif_else.txt";
//        String filename = "./src/test/data/test_elsif_without_else.txt";

        try {
            service.process(filename);
        } catch (Exception e) {
            log.error("ERROR: " + e.getMessage());
        }
    }
}
