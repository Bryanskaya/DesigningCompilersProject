package ru.bmstu.oberoncompiler.antlr4.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;
import ru.bmstu.oberoncompiler.antlr4.config.AppParams;

import java.io.IOException;


@Slf4j
@Component
public class ShutdownListener implements ApplicationListener<ContextClosedEvent> {
    @Autowired
    private AppParams appParams;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        int exitCode;
        String[] cmdArr = {
                String.format("C:\\Program Files\\LLVM\\bin\\clang.exe -c -o " + appParams.buildDir +
                        "mainCRTStartup.obj ./src/main/java/ru/bmstu/oberoncompiler/antlr4/config/mainCRTStartup.c"),
                String.format("C:\\Program Files\\LLVM\\bin\\clang.exe -c -o " + appParams.buildDir + appParams.moduleName + ".obj " +
                        appParams.buildDir + "minimalistic.ll"), //appParams.moduleName),
                String.format("C:\\Program Files\\LLVM\\bin\\lld-link.exe /subsystem:console " +
                        "/out:" + appParams.buildDir + appParams.moduleName + ".exe " +
                        appParams.buildDir + appParams.moduleName + ".obj " +
                        appParams.buildDir + "mainCRTStartup.obj")
        };

        for (String cmd : cmdArr) {
            try {
                exitCode = runCommand(cmd);

                if (exitCode == 0) {
                    log.info("{} was successfully completed.", cmd);
                } else {
                    log.warn("{} was failed.", cmd);
                }
            } catch (Exception e) {
                log.error("ERROR: {}", e.getMessage());
            }
        }
    }

    public static int runCommand(String cmd) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(cmd);
        return process.waitFor();
    }
}




