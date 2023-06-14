package ru.bmstu.oberoncompiler.antlr4;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.llvm.LLVM.*;
import org.bytedeco.llvm.global.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import ru.bmstu.oberoncompiler.antlr4.service.OberonCompilerService;

import static org.bytedeco.llvm.global.LLVM.*;


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
        String filename = "./src/test/data/test_assign_value.txt";
//        String filename = "./src/test/data/test_1.txt";
//        String filename = "./src/test/data/test_several_vars.txt";
//        String filename = "./src/test/data/test_several_types.txt";

        try {
            service.process(filename);
//            testLlvm();
//            testGlobal();
        } catch (Exception e) {
            log.error("ERROR: " + e.getMessage());
        }
    }

    private void testLlvm() {
        LLVMInitializeNativeTarget();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();

        LLVMContextRef context = LLVMContextCreate();
        LLVMModuleRef module = LLVMModuleCreateWithNameInContext("hello", context);

// Создание функции main
        LLVMTypeRef paramTypes = LLVMInt32Type();
        LLVMTypeRef mainFunctionType = LLVMFunctionType(LLVMInt32Type(), paramTypes, 0, 0);
        LLVMValueRef mainFunction = LLVMAddFunction(module, "main", mainFunctionType);
        LLVMSetLinkage(mainFunction, LLVMExternalLinkage);
        LLVMSetFunctionCallConv(mainFunction, LLVMCCallConv);

// Создание блока внутри функции main
        LLVMBasicBlockRef entryBlock = LLVMAppendBasicBlock(mainFunction, "entry");

// Создание объекта LLVMBuilderRef
        LLVMBuilderRef builder = LLVMCreateBuilderInContext(context);
        LLVMPositionBuilderAtEnd(builder, entryBlock);

// Создание локальной переменной
        LLVMValueRef localVar = LLVMBuildAlloca(builder, LLVMInt32Type(), "n");

// Создание константы с значением 1024 и типом intType
        LLVMValueRef value1024 = LLVMConstInt(LLVMInt32Type(), 1024, 1); // 1 означает, что значение знаковое

// Присвоение значения локальной переменной
        LLVMBuildStore(builder, value1024, localVar);

// Возвращение значения из функции main
        LLVMBuildRet(builder, LLVMConstInt(LLVMInt32Type(), 0, 0));

// Генерация кода LLVM IR в текстовый формат
        LLVMDumpModule(module);
    }

    private void testGlobal() {
        LLVMInitializeNativeTarget();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();

        LLVMContextRef context = LLVMContextCreate();
        LLVMModuleRef module = LLVMModuleCreateWithNameInContext("hello", context);

        // Создание функции main
        LLVMTypeRef mainFunctionType = LLVMFunctionType(LLVMInt32Type(), LLVMInt32Type(), 0, 0);
        LLVMValueRef mainFunction = LLVMAddFunction(module, "main", mainFunctionType);

// Создание блока внутри функции main
        LLVMBasicBlockRef entryBlock = LLVMAppendBasicBlock(mainFunction, "entry");


// Создание объекта LLVMBuilderRef
        LLVMBuilderRef builder = LLVMCreateBuilderInContext(context);

        LLVMPositionBuilderAtEnd(builder, entryBlock);


// Создание глобальной переменной
        LLVMValueRef n = LLVMAddGlobal(module, LLVMInt32Type(), "test");

// Создание константы с значением 1024 и типом intType
        LLVMValueRef value1024 = LLVMConstInt(LLVMInt32Type(), 1024, 1); // 1 означает, что значение знаковое

// Присвоение значения переменной nRef
        LLVMBuildStore(builder, value1024, n);

        LLVMDumpModule(module);
    }
}
