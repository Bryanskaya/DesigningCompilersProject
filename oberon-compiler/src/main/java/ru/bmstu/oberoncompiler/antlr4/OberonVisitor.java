package ru.bmstu.oberoncompiler.antlr4;


import lombok.extern.slf4j.Slf4j;
import oberon.antlr4.OberonBaseVisitor;
import oberon.antlr4.OberonParser;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.bmstu.oberoncompiler.antlr4.config.AppParams;
import ru.bmstu.oberoncompiler.antlr4.exception.WrongModuleNameException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.bytedeco.llvm.global.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.LLVMCreateBuilder;

@Slf4j
@Service
public class OberonVisitor extends OberonBaseVisitor {
    @Autowired
    private AppParams appParams;

    private LLVMBuilderRef builder;

    public Object visitModule(OberonParser.ModuleContext ctx) {
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeDisassembler();
        LLVMInitializeNativeTarget();

        LLVMContextRef context = LLVMContextCreate();
        LLVMModuleRef module = LLVMModuleCreateWithNameInContext(appParams.moduleName, context);

        LLVMValueRef function = LLVMAddFunction(module, "main",
                LLVMFunctionType(LLVMInt32Type(), new PointerPointer<>(), 0, 0));

        LLVMBasicBlockRef entry = LLVMAppendBasicBlock(function, "main_entry");

        String nameStartModule = visitIdent(ctx.ident(0));
        if (ctx.importList() != null)
            visitImportList(ctx.importList());

//        visitDeclarationSequence(ctx.declarationSequence()); //TODO return value

        LLVMBuilderRef builderCur = visitStatementSequence(ctx.statementSequence());
        LLVMPositionBuilderAtEnd(builderCur, entry);

        String nameEndModule = visitIdent(ctx.ident(1));
        if (!Objects.equals(nameStartModule, nameEndModule))
            throw new WrongModuleNameException();

        return module;
    }

    public String visitIdent(OberonParser.IdentContext ctx) {
        return ctx.getText();
    }

    public Object visitImportList(OberonParser.ImportListContext ctx) {
        //todo
        return null; //TODO
    }

    /**
     * declarationSequence
     *    : (CONST (constDeclaration ';')*)? (TYPE (typeDeclaration ';')*)? (VAR (variableDeclaration ';')*)? (procedureDeclaration ';')*
     *    ;
     * @param ctx the declarationSequence context
     * @return Object
     */
    public Object visitDeclarationSequence(OberonParser.DeclarationSequenceContext ctx) {
        Object res = null;
        if (ctx.constDeclaration().size() > 0)
            throw new UnsupportedOperationException("CONSTDECLARATION NOT SUPPORTED YET"); //TODO
        if (ctx.typeDeclaration().size() > 0)
            throw new UnsupportedOperationException("TYPEDECLARATION NOT SUPPORTED YET"); //TODO
        if (ctx.variableDeclaration().size() > 0) {
            List<Object> resLst = new ArrayList<>();
            for (OberonParser.VariableDeclarationContext context: ctx.variableDeclaration()) {
                resLst.add(visitVariableDeclaration(context));
            }
            res = resLst; //todo check logic
        }
        if (ctx.procedureDeclaration().size() > 0)
            throw new UnsupportedOperationException("PROCEDUREDECLARATION NOT SUPPORTED YET"); //TODO

        return null; //TODO
    }

    /**
     * variableDeclaration
     *    : identList ':' type_
     *    ;
     * @param ctx variable declaration context
     * @return object
     */
    public Object visitVariableDeclaration(OberonParser.VariableDeclarationContext ctx) {
        Object resIdentList = visitIdentList(ctx.identList()); //todo return value
        Object resType_ = visitType_(ctx.type_()); //todo return value

        return null; //todo
    }

    public Object visitIdentList(OberonParser.IdentListContext ctx) {
        throw new UnsupportedOperationException("IDENTLIST NOT SUPPORTED YET"); //TODO
    }

    public Object visitType_(OberonParser.Type_Context ctx) {
        throw new UnsupportedOperationException("TYPE_ NOT SUPPORTED YET"); //TODO
    }

    public LLVMBuilderRef visitStatementSequence(OberonParser.StatementSequenceContext ctx) {
        LLVMBuilderRef builderCur = LLVMCreateBuilder();
        builder = builderCur;

        for (OberonParser.StatementContext statement: ctx.statement()) {
            visitStatement(statement);
        }

        LLVMValueRef result = LLVMConstNull(LLVMInt32Type());
        LLVMBuildRet(builder, result);

        builder = null;

        return builderCur;
    }

    public Object visitStatement(OberonParser.StatementContext ctx) {
        Object res = null;
        if (ctx.assignment() != null)
            res = visitAssignment(ctx.assignment());
        else if (ctx.procedureCall() != null)
            throw new UnsupportedOperationException("PROCEDURECALL NOT SUPPORTED YET"); //TODO
        else if (ctx.ifStatement() != null)
            throw new UnsupportedOperationException("IFSTATEMENT NOT SUPPORTED YET"); //TODO
        else if (ctx.caseStatement() != null)
            throw new UnsupportedOperationException("CASESTATEMENT NOT SUPPORTED YET"); //TODO
        else if (ctx.whileStatement() != null)
            throw new UnsupportedOperationException("WHILESTATEMENT NOT SUPPORTED YET"); //TODO
        else if (ctx.forStatement() != null)
            throw new UnsupportedOperationException("FORSTATEMENT NOT SUPPORTED YET"); //TODO

        return null; //TODO
    }

    public Object visitAssignment(OberonParser.AssignmentContext ctx) {
        Object resDesignator = visitDesignator(ctx.designator()); //todo return value
        Object resExpression = visitExpression(ctx.expression()); //todo return value
        //llvmstore
        return null; //TODO
    }

    public Object visitDesignator(OberonParser.DesignatorContext ctx) {
        Object resQualident = visitQualident(ctx.qualident());
        for (OberonParser.SelectorContext selector : ctx.selector()) {
            visitSelector(selector); //todo return value
        }

        //todo logic

        return resQualident;
    }

    public Object visitQualident(OberonParser.QualidentContext ctx) {
        return visitIdent(ctx.ident());
    }

    public Object visitSelector(OberonParser.SelectorContext ctx) {
        Object res = null;
        if (ctx.expList() != null)
            res = visitExpList(ctx.expList());  //todo return value

        return res; //todo
    }

    public Object visitExpList(OberonParser.ExpListContext ctx) {
        Object resExp0 = visitExpression(ctx.expression(0)); //todo return value
        for (int i = 1; i < ctx.expression().size(); i++) {
            Object resExp1 = visitExpression(ctx.expression(i)); //todo return value
        }

        return null; //todo
    }

    public Object visitExpression(OberonParser.ExpressionContext ctx) {
        Object resSimpleExp0 = visitSimpleExpression(ctx.simpleExpression(0)); //todo return value

        if (ctx.simpleExpression().size() == 2) {
            Object resRelation = visitRelation(ctx.relation()); //todo return value
            Object resSimpleExp1 = visitSimpleExpression(ctx.simpleExpression(1)); //todo return value

            //todo logic with return values
        }

        return resSimpleExp0; //TODO
    }

    public Object visitSimpleExpression(OberonParser.SimpleExpressionContext ctx) {
        //TODO what should I do with +/-
        Object resTerm0 = visitTerm(ctx.term(0));
        for (int i = 0; i < ctx.addOperator().size(); i++) {
            Object resAddOperator = visitAddOperator(ctx.addOperator(i)); //todo return value
            Object resTerm1 = visitTerm(ctx.term(i + 1)); //todo attention + return value
        }

        return resTerm0; //todo
    }

    public Object visitRelation(OberonParser.RelationContext ctx) {
        throw new UnsupportedOperationException("RELATION NOT SUPPORTED YET"); //todo
    }

    public Object visitTerm(OberonParser.TermContext ctx) {
        Object resFactor0 = visitFactor(ctx.factor(0));
        for (int i = 0; i < ctx.mulOperator().size(); i++) {
            Object resMulOp = visitMulOperator(ctx.mulOperator(i)); //todo return value
            Object resFactor1 = visitFactor(ctx.factor(i + 1)); //todo return value
            //todo logic
        }

        return resFactor0;
    }

    public Object visitAddOperator(OberonParser.AddOperatorContext ctx) {
        throw new UnsupportedOperationException("ADDOPERATOR NOT SUPPORTED YET"); //todo
    }

    public Object visitFactor(OberonParser.FactorContext ctx) {
        Object res;
        if (ctx.number() != null)
            res = visitNumber(ctx.number());
        else if (ctx.set_() != null)
            throw new UnsupportedOperationException("SET_ NOT SUPPORTED YET"); //todo
        else if (ctx.designator() != null) {
            res = visitDesignator(ctx.designator());
        }
        else if (ctx.expression() != null)
            res = visitExpression(ctx.expression());
        else if (ctx.factor() != null)
            res = visitFactor(ctx.factor());
        else
            return ctx.getText(); //todo string, true, false, nil

        return res; //todo
    }

    public LLVMValueRef visitNumber(OberonParser.NumberContext ctx) {
        LLVMValueRef res = null;
        if (ctx.integer() != null)
            res = visitInteger(ctx.integer());
//        else if (ctx.real() != null)
//            res = visitReal(ctx.real()); //todo

        return res;
    }

    public LLVMValueRef visitInteger(OberonParser.IntegerContext ctx) {
        return LLVMConstInt(LLVMInt32Type(), Long.parseLong(ctx.getText()), 1);
    }

    public Object visitReal(OberonParser.RealContext ctx) {
        //todo two DIGIT
//        Object res = visitScaleFactor(ctx.scaleFactor());
        return null; //todo
    }

//    public Object visitScaleFactor(OberonParser.ScaleFactorContext ctx) {
//        return ctx.getText(); //todo check
//    }

    public Object visitMulOperator(OberonParser.MulOperatorContext ctx) {
        throw new UnsupportedOperationException("MULOPERATOR NOT SUPPORTED YET"); //todo
    }
}
