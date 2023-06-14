package ru.bmstu.oberoncompiler.antlr4;


import lombok.extern.slf4j.Slf4j;
import oberon.antlr4.OberonBaseVisitor;
import oberon.antlr4.OberonParser;
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

    private LLVMContextRef context;
    private LLVMBuilderRef builder;
    private LLVMModuleRef module;

    public Object visitModule(OberonParser.ModuleContext ctx) {
//        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
//        LLVMInitializeNativeDisassembler();
        LLVMInitializeNativeTarget();

        context = LLVMContextCreate();
        module = LLVMModuleCreateWithNameInContext(appParams.moduleName, context);

        LLVMValueRef function = LLVMAddFunction(module, "main",
                LLVMFunctionType(LLVMInt32Type(), LLVMInt32Type(), 0, 0));

        LLVMBasicBlockRef entry = LLVMAppendBasicBlock(function, "main_entry");

        LLVMBuilderRef builderCur = LLVMCreateBuilderInContext(context);
        builder = builderCur;
        LLVMPositionBuilderAtEnd(builderCur, entry);

        String nameStartModule = visitIdent(ctx.ident(0));
        if (ctx.importList() != null)
            visitImportList(ctx.importList());

        visitDeclarationSequence(ctx.declarationSequence()); //TODO return value

        visitStatementSequence(ctx.statementSequence());

        LLVMValueRef result = LLVMConstNull(LLVMInt32Type());
        LLVMBuildRet(builderCur, result);

        String nameEndModule = visitIdent(ctx.ident(1));
        if (!Objects.equals(nameStartModule, nameEndModule))
            throw new WrongModuleNameException();

//        builder = null;

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
        if (ctx.constDeclaration().size() > 0)
            throw new UnsupportedOperationException("CONSTDECLARATION NOT SUPPORTED YET"); //TODO
        if (ctx.typeDeclaration().size() > 0)
            throw new UnsupportedOperationException("TYPEDECLARATION NOT SUPPORTED YET"); //TODO
        if (ctx.variableDeclaration().size() > 0) {
            for (OberonParser.VariableDeclarationContext context: ctx.variableDeclaration()) {
                visitVariableDeclaration(context);
            }
        }
        if (ctx.procedureDeclaration().size() > 0)
            throw new UnsupportedOperationException("PROCEDUREDECLARATION NOT SUPPORTED YET"); //TODO

        return null; //TODO
    }

    /**
     * variableDeclaration : identList ':' type_ ;
     * @param ctx variable declaration context
     * @return object
     */
    public Object visitVariableDeclaration(OberonParser.VariableDeclarationContext ctx) {
        List<String> resIdentList = visitIdentList(ctx.identList()); //todo return value
        String resType_ = visitType_(ctx.type_()); //todo return value

        for (String ident : resIdentList) {
            LLVMValueRef value;
            switch (resType_) {
                case "INTEGER":
                    value = LLVMAddGlobal(module, LLVMInt32Type(), ident);
                    LLVMSetInitializer(value, LLVMConstInt(LLVMInt32Type(), 0, 1));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported variable type: " + resType_);
            }
            LLVMSetLinkage(value, LLVMCommonLinkage);
        }

        return null; //todo
    }

    public List<String> visitIdentList(OberonParser.IdentListContext ctx) {
        List<String> resIdentdefLst = new ArrayList<>();
        for (OberonParser.IdentdefContext context : ctx.identdef())
            resIdentdefLst.add(visitIdentdef(context));

        return resIdentdefLst; //todo
    }

    /**
     * type_ : qualident | arrayType | pointerType | procedureType ;
     * @param ctx type context
     * @return Object
     */
    public String visitType_(OberonParser.Type_Context ctx) { //todo type
        String res = null;
        if (ctx.qualident() != null)
            res = visitQualident(ctx.qualident());
        else if (ctx.arrayType() != null)
            throw new UnsupportedOperationException("ARRAYTYPE NOT SUPPORTED YET"); //TODO
        else if (ctx.pointerType() != null)
            throw new UnsupportedOperationException("POINTERTYPE NOT SUPPORTED YET"); //TODO
        else if (ctx.procedureType() != null)
            throw new UnsupportedOperationException("PROCEDURETYPE NOT SUPPORTED YET"); //TODO

        return res; //todo
    }

    /**
     * identdef : ident '*'? ;
     * @param ctx the parse tree
     * @return
     */
    public String visitIdentdef(OberonParser.IdentdefContext ctx) {
        return visitIdent(ctx.ident()); //todo не учла что может быть указатель -- правило *
    }

    public LLVMBuilderRef visitStatementSequence(OberonParser.StatementSequenceContext ctx) {
        for (OberonParser.StatementContext statement: ctx.statement()) {
            visitStatement(statement);
        }

        return null;
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
        String resDesignator = visitDesignator(ctx.designator());

//        LLVMContextRef context1 = LLVMContextCreate();
//        LLVMModuleRef module = LLVMModuleCreateWithNameInContext("hello", context);
//        LLVMBuilderRef builder1 = LLVMCreateBuilderInContext(context1);
//        LLVMTypeRef intType = LLVMInt32TypeInContext(context);
//        LLVMValueRef n = LLVMAddGlobal(module, intType, "test");
//        LLVMValueRef nRef = LLVMGetNamedGlobal(module, "test");
//        LLVMValueRef value1024 = LLVMConstInt(intType, 1024, 1);
//        LLVMBuildStore(builder, value1024, nRef);
//
//        final LLVMValueRef andResultPtr = LLVMBuildAlloca(builder, LLVMInt1Type(), "");
//        LLVMBuildStore(builder, LLVMConstInt(LLVMInt1Type(), 0, 0), andResultPtr);


        LLVMValueRef varRef = LLVMGetNamedGlobal(module, resDesignator);
        if (varRef == null)
            throw new IllegalArgumentException("Wrong variable's name: " + resDesignator);

        LLVMValueRef valueExpressionRef = visitExpression(ctx.expression()); //todo return value

        try {
            LLVMBuildStore(builder, valueExpressionRef, varRef);
        } catch (Throwable e) {
            log.error("///// {}", e.getMessage());
            e.printStackTrace();
        }

        return null; //TODO
    }

    public String visitDesignator(OberonParser.DesignatorContext ctx) {
        String resQualident = visitQualident(ctx.qualident());
        for (OberonParser.SelectorContext selector : ctx.selector()) {
//            visitSelector(selector); //todo return value
            throw new UnsupportedOperationException("SELECTOR NOT SUPPORTED YET");
        }

        return resQualident;
    }

    public String visitQualident(OberonParser.QualidentContext ctx) {
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

    public LLVMValueRef visitExpression(OberonParser.ExpressionContext ctx) {
        LLVMValueRef resSimpleExp0 = visitSimpleExpression(ctx.simpleExpression(0)); //todo return value

        if (ctx.simpleExpression().size() == 2) {
            Object resRelation = visitRelation(ctx.relation()); //todo return value
            LLVMValueRef resSimpleExp1 = visitSimpleExpression(ctx.simpleExpression(1)); //todo return value

            //todo logic with return values
        }

        return resSimpleExp0; //TODO
    }

    public LLVMValueRef visitSimpleExpression(OberonParser.SimpleExpressionContext ctx) {
        //TODO what should I do with +/-
        LLVMValueRef resTerm0 = visitTerm(ctx.term(0));
        for (int i = 0; i < ctx.addOperator().size(); i++) {
            Object resAddOperator = visitAddOperator(ctx.addOperator(i)); //todo return value
            LLVMValueRef resTerm1 = visitTerm(ctx.term(i + 1)); //todo attention + return value
        }

        return resTerm0; //todo
    }

    public Object visitRelation(OberonParser.RelationContext ctx) {
        throw new UnsupportedOperationException("RELATION NOT SUPPORTED YET"); //todo
    }

    public LLVMValueRef visitTerm(OberonParser.TermContext ctx) {
        LLVMValueRef resFactor0 = visitFactor(ctx.factor(0));
        for (int i = 0; i < ctx.mulOperator().size(); i++) {
            Object resMulOp = visitMulOperator(ctx.mulOperator(i)); //todo return value
            LLVMValueRef resFactor1 = visitFactor(ctx.factor(i + 1)); //todo return value
            //todo logic
        }

        return resFactor0;
    }

    public Object visitAddOperator(OberonParser.AddOperatorContext ctx) {
        throw new UnsupportedOperationException("ADDOPERATOR NOT SUPPORTED YET"); //todo
    }

    public LLVMValueRef visitFactor(OberonParser.FactorContext ctx) {
        LLVMValueRef res;
        if (ctx.number() != null)
            res = visitNumber(ctx.number());
        else if (ctx.set_() != null)
            throw new UnsupportedOperationException("SET_ NOT SUPPORTED YET"); //todo
        else if (ctx.designator() != null) {
//            res = visitDesignator(ctx.designator());
            throw new UnsupportedOperationException("DESIGNATOR IN FACTOR NOT SUPPORTED YET"); //TODO
        }
        else if (ctx.expression() != null)
//            res = visitExpression(ctx.expression());
            throw new UnsupportedOperationException("EXPRESSION IN FACTOR NOT SUPPORTED YET"); //TODO
        else if (ctx.factor() != null)
            res = visitFactor(ctx.factor());
        else
            throw new UnsupportedOperationException("STRING, TRUE, FALSE, NIL IN FACTOR NOT SUPPORTED YET"); //TODO
//            return ctx.getText(); //todo string, true, false, nil

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
