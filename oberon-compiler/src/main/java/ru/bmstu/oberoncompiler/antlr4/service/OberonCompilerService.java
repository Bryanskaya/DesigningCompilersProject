package ru.bmstu.oberoncompiler.antlr4.service;

import lombok.extern.slf4j.Slf4j;
import oberon.antlr4.OberonLexer;
import oberon.antlr4.OberonParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.bytedeco.javacpp.*;
import org.bytedeco.llvm.LLVM.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.bmstu.oberoncompiler.antlr4.OberonWalker;
import ru.bmstu.oberoncompiler.antlr4.config.AppParams;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.bytedeco.llvm.global.LLVM.*;
import static ru.bmstu.oberoncompiler.antlr4.service.ShutdownListener.runCommand;

@Slf4j
@Service
public class OberonCompilerService {
    private OberonLexer lexer;
    private LLVMBuilderRef builder;
    private LLVMContextRef context;
    private LLVMModuleRef module;

    @Autowired
    private AppParams appParams;

    public void process(String filename) throws IOException, InterruptedException {
        lexer = doLexicalAnalysis(filename);
        OberonParser parser = doSynAnalysis();
        ParseTree tree = parser.module();

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new OberonWalker(), tree);

        module = createModule();

        // Вывод LLVM-IR на консоль
        LLVMDumpModule(module);

        // Запись модуля в LLVM Assembly файл
        LLVMPrintModuleToFile(module, appParams.buildDir + appParams.moduleName + ".ll", (ByteBuffer) null);

        runCommand(String.format("C:\\Program Files\\LLVM\\bin\\clang.exe -c -o %s %s",
                appParams.buildDir + appParams.moduleName + ".obj",
                appParams.buildDir + appParams.moduleName + ".ll"));

        freeResources();
    }

    private OberonLexer doLexicalAnalysis(String filename) throws IOException {
        return new OberonLexer(CharStreams.fromFileName(filename));
    }

    private OberonParser doSynAnalysis() {
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        return new OberonParser(tokens);
    }

    private LLVMModuleRef createModule() {
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeDisassembler();
        LLVMInitializeNativeTarget();

        context = LLVMContextCreate();
        LLVMModuleRef module = LLVMModuleCreateWithNameInContext(appParams.moduleName, context);

        LLVMTypeRef[] paramTypes = { LLVMInt32Type(), LLVMInt32Type() };
        LLVMTypeRef functionType = LLVMFunctionType(LLVMInt32Type(), new PointerPointer<>(paramTypes), 2, 0);
        LLVMValueRef function = LLVMAddFunction(module, "add", functionType);

        // Создание базового блока
        LLVMBasicBlockRef entry = LLVMAppendBasicBlock(function, "entry");
        builder = LLVMCreateBuilder();
        LLVMPositionBuilderAtEnd(builder, entry);

        // Генерация инструкций
        LLVMValueRef param1 = LLVMGetParam(function, 0);
        LLVMValueRef param2 = LLVMGetParam(function, 1);
        LLVMValueRef result = LLVMBuildAdd(builder, param1, param2, "result");
        LLVMBuildRet(builder, result);

        return module;
    }

    private void freeResources() {
        LLVMDisposeBuilder(builder);
        LLVMDisposeModule(module);
        LLVMContextDispose(context);
    }
}
