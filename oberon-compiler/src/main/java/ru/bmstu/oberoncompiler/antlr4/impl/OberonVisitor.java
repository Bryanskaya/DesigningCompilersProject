package ru.bmstu.oberoncompiler.antlr4.impl;


import lombok.extern.slf4j.Slf4j;
import oberon.antlr4.OberonBaseVisitor;
import oberon.antlr4.OberonParser;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.*;
import org.bytedeco.llvm.global.LLVM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.bmstu.oberoncompiler.antlr4.config.AppParams;
import ru.bmstu.oberoncompiler.antlr4.exception.WrongModuleNameException;
import ru.bmstu.oberoncompiler.antlr4.utils.QuadFunction;

import java.util.ArrayList;
import java.util.HashMap;
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
    private LLVMValueRef function;
    private HashMap<String, LLVMTypeRef> varTypeMap = new HashMap<>();

    private void initLLVMComponents() {
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeDisassembler();
        LLVMInitializeNativeTarget();
    }

    private void initMain() {
        module = LLVMModuleCreateWithNameInContext(appParams.moduleName, context);

        function = createFunc("main", LLVMInt32Type(), LLVMInt32Type());

        LLVMBasicBlockRef entry = LLVMAppendBasicBlock(function, "main_entry");

        builder = LLVMCreateBuilderInContext(context);
        LLVMPositionBuilderAtEnd(builder, entry);
    }

    private LLVMValueRef createFunc(String funcName, LLVMTypeRef paramType, LLVMTypeRef returnType) {
        LLVMTypeRef functionType = LLVMFunctionType(returnType, paramType, 0, 0);

        return LLVMAddFunction(module, funcName, functionType);
    }

    public Object visitModule(OberonParser.ModuleContext ctx) {
        initLLVMComponents();
        initMain();

        String nameStartModule = visitIdent(ctx.ident(0));

        visitDeclarationSequence(ctx.declarationSequence());

        visitStatementSequence(ctx.statementSequence());

        LLVMValueRef returnValue = visitFactor(ctx.factor());

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

    /**
     * declarationSequence
     *    : (CONST (constDeclaration ';')*)? (TYPE (typeDeclaration ';')*)? (VAR (variableDeclaration ';')*)? (procedureDeclaration ';')*
     *    ;
     * @param ctx the declarationSequence context
     * @return Object
     */
    public Object visitDeclarationSequence(OberonParser.DeclarationSequenceContext ctx) {
        if (ctx.constDeclaration().size() > 0)
            throw new UnsupportedOperationException("CONSTDECLARATION NOT SUPPORTED YET");
        if (ctx.typeDeclaration().size() > 0)
            throw new UnsupportedOperationException("TYPEDECLARATION NOT SUPPORTED YET");
        if (ctx.variableDeclaration().size() > 0) {
            for (OberonParser.VariableDeclarationContext context: ctx.variableDeclaration()) {
                visitVariableDeclaration(context);
            }
        }

        return null;
    }

    /**
     * variableDeclaration : identList ':' type_ ;
     * @param ctx variable declaration context
     * @return object
     */
    public Object visitVariableDeclaration(OberonParser.VariableDeclarationContext ctx) {
        List<String> resIdentList = visitIdentList(ctx.identList());
        LLVMTypeRef resType_ = visitType_(ctx.type_());

        for (String ident : resIdentList) {
            LLVMValueRef value = LLVMAddGlobal(module, resType_, ident);

            LLVMSetInitializer(value, LLVMConstNull(resType_));

            LLVMSetLinkage(value, LLVMCommonLinkage);
            varTypeMap.put(ident, resType_);
        }

        return null;
    }

    public List<String> visitIdentList(OberonParser.IdentListContext ctx) {
        List<String> resIdentdefLst = new ArrayList<>();
        for (OberonParser.IdentdefContext context : ctx.identdef())
            resIdentdefLst.add(visitIdentdef(context));

        return resIdentdefLst;
    }

    public LLVMTypeRef visitType_(OberonParser.Type_Context ctx) {
        LLVMTypeRef res;
        if (ctx.qualident() != null) {
            String typeStr = visitQualident(ctx.qualident());
            res = switch (typeStr) {
                case "INTEGER" -> LLVMInt32Type();
                case "REAL" -> LLVMDoubleType();
                default -> throw new IllegalArgumentException("Unsupported variable type: " + typeStr);
            };
        }
        else if (ctx.arrayType() != null)
            res = visitArrayType(ctx.arrayType());
        else
            throw new UnsupportedOperationException("type_: MENTIONED TYPE NOT SUPPORTED YET");

        return res;
    }

    public LLVMTypeRef visitArrayType(OberonParser.ArrayTypeContext ctx) {
        LLVMValueRef lengthRef = visitLength(ctx.length());
        LLVMTypeRef type_ = visitType_(ctx.type_());

        return LLVMArrayType(type_, (int) LLVMConstIntGetSExtValue(lengthRef));
    }

    public LLVMValueRef visitLength(OberonParser.LengthContext ctx) {
        return visitConstExpression(ctx.constExpression());
    }

    public LLVMValueRef visitConstExpression(OberonParser.ConstExpressionContext ctx) {
        return visitExpression(ctx.expression());
    }

    public String visitIdentdef(OberonParser.IdentdefContext ctx) {
        return visitIdent(ctx.ident());
    }

    public LLVMValueRef visitStatementSequence(OberonParser.StatementSequenceContext ctx) {
        for (OberonParser.StatementContext statement: ctx.statement()) {
            visitStatement(statement);
        }

        return null;
    }

    public Object visitStatement(OberonParser.StatementContext ctx) {
        if (ctx.assignment() != null)
            visitAssignment(ctx.assignment());
        else if (ctx.ifStatement() != null)
            visitIfStatement(ctx.ifStatement());
        else if (ctx.whileStatement() != null)
            visitWhileStatement(ctx.whileStatement());
        else if (ctx.forStatement() != null)
            throw new UnsupportedOperationException("FORSTATEMENT NOT SUPPORTED YET");

        return null;
    }

    public Object visitAssignment(OberonParser.AssignmentContext ctx) {
        LLVMValueRef varRef = visitDesignator(ctx.designator());
        LLVMValueRef valueExpressionRef = visitExpression(ctx.expression());

        LLVMBuildStore(builder, valueExpressionRef, varRef);

        return null;
    }

    public Object visitIfStatement(OberonParser.IfStatementContext ctx) {
        LLVMBasicBlockRef thenBlock;
        LLVMBasicBlockRef nextBlock = LLVMAppendBasicBlock(function, "next");
        LLVMBasicBlockRef endBlock = LLVMAppendBasicBlock(function, "end");

        LLVMBuildBr(builder, nextBlock);

        for (int i = 0; i < ctx.expression().size(); i++) {
            LLVMPositionBuilderAtEnd(builder, nextBlock);

            thenBlock = LLVMAppendBasicBlock(function, "then");;
            nextBlock = LLVMAppendBasicBlock(function, "next");

            // if
            LLVMValueRef expCondRef = visitExpression(ctx.expression(i));
            LLVMBuildCondBr(builder, expCondRef, thenBlock, nextBlock);

            // then
            LLVMPositionBuilderAtEnd(builder, thenBlock);
            visitStatementSequence(ctx.statementSequence(i));
            LLVMBuildBr(builder, endBlock);
        }

        // else
        LLVMPositionBuilderAtEnd(builder, nextBlock);
        int numStatementSequence = ctx.statementSequence().size();
        int numExpression = ctx.expression().size();
        if (numStatementSequence != numExpression) {
            visitStatementSequence(ctx.statementSequence(numStatementSequence - 1));
        }
        LLVMBuildBr(builder, endBlock);

        LLVMPositionBuilderAtEnd(builder, endBlock);

        return null;
    }

    public Object visitWhileStatement(OberonParser.WhileStatementContext ctx) {
        LLVMBasicBlockRef whileBlock = LLVMAppendBasicBlock(function, "while");
        LLVMBasicBlockRef doBlock = LLVMAppendBasicBlock(function, "do");
        LLVMBasicBlockRef endBlock = LLVMAppendBasicBlock(function, "end");

        LLVMBuildBr(builder, whileBlock);

        // while
        LLVMPositionBuilderAtEnd(builder, whileBlock);
        LLVMValueRef expCondRef = visitExpression(ctx.expression(0));
        LLVMBuildCondBr(builder, expCondRef, doBlock, endBlock);

        // do
        LLVMPositionBuilderAtEnd(builder, doBlock);
        visitStatementSequence(ctx.statementSequence(0));
        LLVMBuildBr(builder, whileBlock);

        if (ctx.expression().size() > 1)
            throw new UnsupportedOperationException("SEVERAL expressions NOT SUPPORTED YET");

        LLVMPositionBuilderAtEnd(builder, endBlock);

        return null;
    }

    public LLVMValueRef visitDesignator(OberonParser.DesignatorContext ctx) {
        String resQualident = visitQualident(ctx.qualident());

        LLVMValueRef varRef = LLVMGetNamedGlobal(module, resQualident);
        if (varRef == null)
            throw new IllegalArgumentException("Wrong variable's name: " + resQualident);

        LLVMValueRef valueSelector;
        for (OberonParser.SelectorContext selector : ctx.selector()) {
            valueSelector = visitSelector(selector);

            if (valueSelector != null) {
                if (valueSelector instanceof ValueRef) {
                    LLVMTypeRef loadType = ((ValueRef) valueSelector).elemType;
                    valueSelector = LLVMBuildLoad2(builder, loadType, valueSelector, "value");
                }

                LLVMValueRef[] indexArr = {
                        LLVMConstInt(LLVMInt32Type(), 0, 0),
                        valueSelector
                };

                varRef = LLVMBuildGEP2(builder, varTypeMap.get(resQualident), varRef,
                        new PointerPointer<>(indexArr), indexArr.length, "arr_element_ptr");
                varRef = new ValueRef(varRef, LLVMInt32Type());
            }

            //todo не очень корректно работает
        }

        return varRef;
    }

    public String visitQualident(OberonParser.QualidentContext ctx) {
        return visitIdent(ctx.ident());
    }

    public LLVMValueRef visitSelector(OberonParser.SelectorContext ctx) {
        LLVMValueRef res = null;
        if (ctx.expList() != null)
            res = visitExpList(ctx.expList());

        return res;
    }

    public LLVMValueRef visitExpList(OberonParser.ExpListContext ctx) {
        LLVMValueRef expLeft = visitExpression(ctx.expression(0));
        for (int i = 1; i < ctx.expression().size(); i++) {
            Object expRight = visitExpression(ctx.expression(i)); //todo return value
        }

        return expLeft;
    }

    public LLVMValueRef visitExpression(OberonParser.ExpressionContext ctx) {
        LLVMValueRef resSimpleExpLeft = visitSimpleExpression(ctx.simpleExpression(0));

        if (ctx.simpleExpression().size() == 2) {
            String resRelation = visitRelation(ctx.relation());
            LLVMValueRef resSimpleExpRight = visitSimpleExpression(ctx.simpleExpression(1));

            resSimpleExpLeft = switch (resRelation) {
                case "=" -> LLVMBuildICmp(builder, LLVMIntEQ, resSimpleExpLeft, resSimpleExpRight, "cmp_equal");
                case "#" -> LLVMBuildICmp(builder, LLVMIntNE, resSimpleExpLeft, resSimpleExpRight, "cmp_unequal");
                case "<" -> LLVMBuildICmp(builder, LLVMIntSLT, resSimpleExpLeft, resSimpleExpRight, "cmp_smaller");
                case "<=" -> LLVMBuildICmp(builder, LLVMIntSLE, resSimpleExpLeft, resSimpleExpRight, "cmp_smaller_or_equal");
                case ">" -> LLVMBuildICmp(builder, LLVMIntSGT, resSimpleExpLeft, resSimpleExpRight, "cmp_bigger");
                case ">=" -> LLVMBuildICmp(builder, LLVMIntSGE, resSimpleExpLeft, resSimpleExpRight, "cmp_bigger_or_equal");
                default -> throw new UnsupportedOperationException("Operator " + resRelation + " NOT SUPPORTED YET");
            };
        }

        return resSimpleExpLeft;
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

    public String visitRelation(OberonParser.RelationContext ctx) {
        return ctx.getText();
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
                case "&" -> LLVMBuildAnd(builder, resFactorLeft, resFactorRight, "and_condition");
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
            throw new UnsupportedOperationException("SET_ NOT SUPPORTED YET");
        else if (ctx.designator() != null) {
            LLVMValueRef varRef = visitDesignator(ctx.designator());
            LLVMTypeRef loadType = (varRef instanceof ValueRef) ?
                    ((ValueRef) varRef).elemType : LLVMTypeOf(LLVMGetOperand(varRef, 0));

            res = LLVMBuildLoad2(builder, loadType, varRef, "value");
        }
        else if (ctx.expression() != null)
            res = visitExpression(ctx.expression());
        else if (ctx.factor() != null)
            res = visitFactor(ctx.factor());
        else
            throw new UnsupportedOperationException("STRING, TRUE, FALSE, NIL IN FACTOR NOT SUPPORTED YET");
//            return ctx.getText();

        return res;
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
