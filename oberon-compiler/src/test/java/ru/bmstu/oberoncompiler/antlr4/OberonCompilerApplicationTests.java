package ru.bmstu.oberoncompiler.antlr4;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.bmstu.oberoncompiler.antlr4.config.AppParams;
import ru.bmstu.oberoncompiler.antlr4.service.OberonCompilerService;
import ru.bmstu.oberoncompiler.antlr4.service.ShutdownListener;

import java.io.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test")
public class OberonCompilerApplicationTests {
    @Autowired
    private OberonCompilerService service;
    @Autowired
    private ShutdownListener listener;
    @Autowired
    private AppParams appParams;

    static Stream<Arguments> testAssignAddInt() {
        return Stream.of(
                Arguments.of("./src/test/data/test_assign_add.txt", "./src/test/data/test_assign_add_expected.txt"),
                Arguments.of("./src/test/data/test_assign_array_complicated.txt", "./src/test/data/test_assign_array_complicated_expected.txt"),
                Arguments.of("./src/test/data/test_div_divisible_positive_greater.txt", "./src/test/data/test_div_divisible_positive_greater_expected.txt"),
                Arguments.of("./src/test/data/test_div_divisible_positive_less.txt", "./src/test/data/test_div_divisible_positive_less_expected.txt"),
                Arguments.of("./src/test/data/test_div_divisible_negative_greater.txt", "./src/test/data/test_div_divisible_negative_greater_expected.txt"),
                Arguments.of("./src/test/data/test_div_divisible_zero.txt", "./src/test/data/test_div_divisible_zero_expected.txt"),
                Arguments.of("./src/test/data/test_mod_divisible_greater.txt", "./src/test/data/test_mod_divisible_greater_expected.txt"),
                Arguments.of("./src/test/data/test_mod_divisible_less.txt", "./src/test/data/test_mod_divisible_less_expected.txt"),
                Arguments.of("./src/test/data/test_mod_divisible_zero.txt", "./src/test/data/test_mod_divisible_zero_expected.txt"),
                Arguments.of("./src/test/data/test_assign_minus.txt", "./src/test/data/test_assign_minus_expected.txt"),
                Arguments.of("./src/test/data/test_assign_mul.txt", "./src/test/data/test_assign_mul_expected.txt"),
                Arguments.of("./src/test/data/test_assign_subtract.txt", "./src/test/data/test_assign_subtract_expected.txt"),
                Arguments.of("./src/test/data/test_brackets_v1.txt", "./src/test/data/test_brackets_v1_expected.txt"),
                Arguments.of("./src/test/data/test_brackets_v2.txt", "./src/test/data/test_brackets_v2_expected.txt"),
                Arguments.of("./src/test/data/test_brackets_v3.txt", "./src/test/data/test_brackets_v3_expected.txt"),
                Arguments.of("./src/test/data/test_if_else.txt", "./src/test/data/test_if_else_expected.txt"),
                Arguments.of("./src/test/data/test_if_else_inner.txt", "./src/test/data/test_if_else_inner_expected.txt"),
                Arguments.of("./src/test/data/test_while.txt", "./src/test/data/test_while_expected.txt"),
                Arguments.of("./src/test/data/test_fib.txt", "./src/test/data/test_fib_expected.txt"),
                Arguments.of("./src/test/data/test_fib_array.txt", "./src/test/data/test_fib_array_expected.txt"),
                Arguments.of("./src/test/data/test_elsif_else.txt", "./src/test/data/test_elsif_else_expected.txt"),
                Arguments.of("./src/test/data/test_elsif_without_else.txt", "./src/test/data/test_elsif_without_else_expected.txt"),
                Arguments.of("./src/test/data/test_if_without_else.txt", "./src/test/data/test_if_without_else_expected.txt")
                );
    }

    private Integer readFile(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        Integer currentLine = Integer.valueOf(reader.readLine());
        reader.close();

        return currentLine;
    }

    private int runTest(String filename) throws IOException, InterruptedException {
        service.process(filename);
        listener.createExeFromLL();

        Process process = Runtime.getRuntime().exec(appParams.buildDir + appParams.moduleName + ".exe");
        process.waitFor();

        return process.exitValue();
    }

    @ParameterizedTest
    @MethodSource
    void testAssignAddInt(String filenameTest, String filenameExpected) throws IOException, InterruptedException {
        int actualRes = runTest(filenameTest);
        int expectedRes = readFile(filenameExpected);

        assertEquals(expectedRes, actualRes);
    }

}
