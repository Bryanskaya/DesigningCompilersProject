package ru.bmstu.oberoncompiler.antlr4.impl;


import lombok.extern.slf4j.Slf4j;
import oberon.antlr4.OberonBaseVisitor;
import oberon.antlr4.OberonParser;
import org.bytedeco.llvm.LLVM.*;
import org.bytedeco.llvm.global.LLVM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.bmstu.oberoncompiler.antlr4.config.AppParams;
import ru.bmstu.oberoncompiler.antlr4.exception.WrongModuleNameException;
import ru.bmstu.oberoncompiler.antlr4.utils.QuadFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.bytedeco.llvm.global.LLVM.*;

@Slf4j
@Service
public class OberonVisitor extends OberonBaseVisitor {
    @Autowired
    private AppParams appParams;

    private final LLVMContextRef context = LLVMContextCreate();;
    private LLVMBuilderRef builder;
    private LLVMModuleRef module;

    private void initLLVMComponents() {
//        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
//        LLVMInitializeNativeDisassembler();
        LLVMInitializeNativeTarget();
    }

    private void initMain() {
        module = LLVMModuleCreateWithNameInContext(appParams.moduleName, context);

        LLVMValueRef function = LLVMAddFunction(module, "main",
                LLVMFunctionType(LLVMInt32Type(), LLVMInt32Type(), 0, 0));

        LLVMBasicBlockRef entry = LLVMAppendBasicBlock(function, "main_entry");

        builder = LLVMCreateBuilderInContext(context);
        LLVMPositionBuilderAtEnd(builder, entry);
    }

    public Object visitModule(OberonParser.ModuleContext ctx) {
        initLLVMComponents();
        initMain();

        String nameStartModule = visitIdent(ctx.ident(0));

        if (ctx.importList() != null)
            visitImportList(ctx.importList());

        visitDeclarationSequence(ctx.declarationSequence()); //TODO return value

        visitStatementSequence(ctx.statementSequence());

        LLVMValueRef returnValue = visitFactor(ctx.factor());

//        LLVMValueRef result = LLVMConstNull(LLVMInt32Type());
        LLVMBuildRet(builder, returnValue);

        String nameEndModule = visitIdent(ctx.ident(1));
        if (!Objects.equals(nameStartModule, nameEndModule))
            throw new WrongModuleNameException(nameStartModule, nameEndModule);

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
                case "REAL":
                    LLVMTypeRef doubleType = LLVMDoubleType();
                    value = LLVMAddGlobal(module, doubleType, ident);
                    LLVMValueRef initValue = LLVMConstReal(doubleType, 0);
                    LLVMSetInitializer(value, initValue);
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
        LLVMValueRef varRef = visitDesignator(ctx.designator());
        LLVMValueRef valueExpressionRef = visitExpression(ctx.expression()); //todo return value

        LLVMBuildStore(builder, valueExpressionRef, varRef);

        return null; //TODO
    }

    public LLVMValueRef visitDesignator(OberonParser.DesignatorContext ctx) {
        String resQualident = visitQualident(ctx.qualident());
        for (OberonParser.SelectorContext selector : ctx.selector()) {
//            visitSelector(selector); //todo return value
            throw new UnsupportedOperationException("SELECTOR NOT SUPPORTED YET");
        }

        LLVMValueRef varRef = LLVMGetNamedGlobal(module, resQualident);
        if (varRef == null)
            throw new IllegalArgumentException("Wrong variable's name: " + resQualident);

        return varRef;
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
        LLVMValueRef resSimpleExpLeft = visitSimpleExpression(ctx.simpleExpression(0));

        if (ctx.simpleExpression().size() == 2) {
            Object resRelation = visitRelation(ctx.relation()); //todo return value
            LLVMValueRef resSimpleExpRight = visitSimpleExpression(ctx.simpleExpression(1)); //todo return value

            //todo logic with return values
        }

        return resSimpleExpLeft; //TODO
    }

    public LLVMValueRef visitSimpleExpression(OberonParser.SimpleExpressionContext ctx) {
        char signChar = ctx.getText().charAt(0);

        LLVMValueRef resTermLeft = visitTerm(ctx.term(0));

        if (signChar == '-')
            resTermLeft = invert(resTermLeft);

        for (int i = 0; i < ctx.addOperator().size(); i++) {
            String resAddOperator = visitAddOperator(ctx.addOperator(i));
            LLVMValueRef resTermRight = visitTerm(ctx.term(i + 1));

            resTermLeft = switch (resAddOperator) {
                case "+" -> mathOperationIntRealBoth(resTermLeft, resTermRight, LLVM::LLVMBuildAdd, LLVM::LLVMBuildFAdd);
                case "-" -> mathOperationIntRealBoth(resTermLeft, resTermRight, LLVM::LLVMBuildSub, LLVM::LLVMBuildFSub);
                default -> throw new UnsupportedOperationException("Operator " + resAddOperator + " NOT SUPPORTED YET");
            };
        }

        return resTermLeft;
    }
    private LLVMValueRef invert(LLVMValueRef term) {
        LLVMTypeRef typeLeftRef = LLVMTypeOf(term);
        if (LLVMGetTypeKind(typeLeftRef) == LLVMIntegerTypeKind) {
            LLVMValueRef minusOne = LLVMConstInt(typeLeftRef, -1, 0);
            term = LLVMBuildMul(builder, term, minusOne, "mul_minus_one");
        }
        else {
            LLVMValueRef minusOne = LLVMConstReal(typeLeftRef, -1.0);
            term = LLVMBuildFMul(builder, term, minusOne, "mul_minus_one");
        }

        return term;
    }
    private LLVMValueRef mathOperationIntRealBoth(LLVMValueRef termLeft, LLVMValueRef termRight,
                                                  QuadFunction<LLVMBuilderRef, LLVMValueRef, LLVMValueRef, String, LLVMValueRef> funcInt,
                                                  QuadFunction<LLVMBuilderRef, LLVMValueRef, LLVMValueRef, String, LLVMValueRef> funcReal) {
        LLVMTypeRef typeLeftRef = LLVMTypeOf(termLeft);
        LLVMTypeRef typeRightRef = LLVMTypeOf(termRight);
        int kindTypeLeft = LLVMGetTypeKind(typeLeftRef);
        int kindTypeRight = LLVMGetTypeKind(typeRightRef);

        if (kindTypeLeft == kindTypeRight && kindTypeLeft == LLVMIntegerTypeKind)
            termLeft = funcInt.apply(builder, termLeft, termRight, "math_operation_int");
        else {
            if (kindTypeLeft == LLVMIntegerTypeKind)
                termLeft = LLVMBuildSIToFP(builder, termLeft, LLVMDoubleType(), "upd_type");
            else if (kindTypeRight == LLVMIntegerTypeKind)
                termRight = LLVMBuildSIToFP(builder, termRight, LLVMDoubleType(), "upd_type");
            termLeft = funcReal.apply(builder, termLeft, termRight, "math_operation_real");
        }

        return termLeft;
    }

    public Object visitRelation(OberonParser.RelationContext ctx) {
        throw new UnsupportedOperationException("RELATION NOT SUPPORTED YET"); //todo
    }

    public LLVMValueRef visitTerm(OberonParser.TermContext ctx) {
        LLVMValueRef resFactorLeft = visitFactor(ctx.factor(0));
        for (int i = 0; i < ctx.mulOperator().size(); i++) {
            String resMulOp = visitMulOperator(ctx.mulOperator(i));
            LLVMValueRef resFactorRight = visitFactor(ctx.factor(i + 1));

            resFactorLeft = switch (resMulOp) {
                case "*" -> mathOperationIntRealBoth(resFactorLeft, resFactorRight, LLVM::LLVMBuildMul, LLVM::LLVMBuildFMul);
                case "/" -> mathOperationRealBoth(resFactorLeft, resFactorRight, LLVM::LLVMBuildFDiv);
                case "DIV" -> mathOperationInt(resFactorLeft, resFactorRight, LLVM::LLVMBuildSDiv);
                case "MOD" -> mathOperationInt(resFactorLeft, resFactorRight, LLVM::LLVMBuildSRem);
                case "&" -> throw new UnsupportedOperationException("& OPERATION IN TERM NOT SUPPORTED YET");
                default -> throw new UnsupportedOperationException("Operator " + resMulOp + " NOT SUPPORTED YET");
            };
        }

        return resFactorLeft;
    }
    private LLVMValueRef mathOperationRealBoth(LLVMValueRef termLeft, LLVMValueRef termRight,
                                               QuadFunction<LLVMBuilderRef, LLVMValueRef, LLVMValueRef, String, LLVMValueRef> func) {
        LLVMTypeRef typeLeftRef = LLVMTypeOf(termLeft);
        LLVMTypeRef typeRightRef = LLVMTypeOf(termRight);
        int kindTypeLeft = LLVMGetTypeKind(typeLeftRef);
        int kindTypeRight = LLVMGetTypeKind(typeRightRef);

        if (kindTypeLeft == kindTypeRight && kindTypeLeft == LLVMIntegerTypeKind)
            throw new IllegalArgumentException("Operand / is not used for operators of INTEGER type");
        else {
            if (kindTypeLeft == LLVMIntegerTypeKind)
                termLeft = LLVMBuildSIToFP(builder, termLeft, LLVMDoubleType(), "upd_type");
            else if (kindTypeRight == LLVMIntegerTypeKind)
                termRight = LLVMBuildSIToFP(builder, termRight, LLVMDoubleType(), "upd_type");
            termLeft = func.apply(builder, termLeft, termRight, "math_operation_real");
        }

        return termLeft;
    }
    private LLVMValueRef mathOperationInt(LLVMValueRef termLeft, LLVMValueRef termRight,
                                          QuadFunction<LLVMBuilderRef, LLVMValueRef, LLVMValueRef, String, LLVMValueRef> func) {
        LLVMTypeRef typeLeftRef = LLVMTypeOf(termLeft);
        LLVMTypeRef typeRightRef = LLVMTypeOf(termRight);
        int kindTypeLeft = LLVMGetTypeKind(typeLeftRef);
        int kindTypeRight = LLVMGetTypeKind(typeRightRef);

        if (kindTypeLeft == kindTypeRight && kindTypeLeft == LLVMIntegerTypeKind)
            termLeft = func.apply(builder, termLeft, termRight, "math_operation_int");
        else {
            throw new UnsupportedOperationException("Operand DIV is not used for operators of REAL type");
        }

        return termLeft;
    }

    public String visitAddOperator(OberonParser.AddOperatorContext ctx) {
        return ctx.getText();
    }

    public LLVMValueRef visitFactor(OberonParser.FactorContext ctx) {
        LLVMValueRef res;
        if (ctx.number() != null)
            res = visitNumber(ctx.number());
        else if (ctx.set_() != null)
            throw new UnsupportedOperationException("SET_ NOT SUPPORTED YET"); //todo
        else if (ctx.designator() != null) {
            LLVMValueRef varRef = visitDesignator(ctx.designator());
            String varName = LLVMGetValueName(varRef).getString();
            res = LLVMBuildLoad2(builder, LLVMTypeOf(LLVMGetOperand(varRef, 0)), varRef, varName + "_value");
        }
        else if (ctx.expression() != null)
            res = visitExpression(ctx.expression());
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
        else if (ctx.real() != null)
            res = visitReal(ctx.real());
        return res;
    }

    public LLVMValueRef visitInteger(OberonParser.IntegerContext ctx) {
        return LLVMConstInt(LLVMInt32Type(), Long.parseLong(ctx.getText()), 1);
    }

    public LLVMValueRef visitReal(OberonParser.RealContext ctx) {
        return LLVMConstReal(LLVMDoubleType(), Double.parseDouble(ctx.getText()));
    }

    public String visitMulOperator(OberonParser.MulOperatorContext ctx) {
        return ctx.getText();
    }
}
