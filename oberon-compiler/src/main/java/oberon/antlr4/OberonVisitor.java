// Generated from D:/BMSTU/DesigningCompilersProject/oberon-compiler/src/main/antlr4/oberon/antlr4\Oberon.g4 by ANTLR 4.12.0
package oberon.antlr4;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link OberonParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface OberonVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link OberonParser#ident}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdent(OberonParser.IdentContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#qualident}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualident(OberonParser.QualidentContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#identdef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentdef(OberonParser.IdentdefContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#integer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInteger(OberonParser.IntegerContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#real}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReal(OberonParser.RealContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#number}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumber(OberonParser.NumberContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#constDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstDeclaration(OberonParser.ConstDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#constExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstExpression(OberonParser.ConstExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#typeDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeDeclaration(OberonParser.TypeDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#type_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType_(OberonParser.Type_Context ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#arrayType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayType(OberonParser.ArrayTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#length}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLength(OberonParser.LengthContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#identList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentList(OberonParser.IdentListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#variableDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDeclaration(OberonParser.VariableDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(OberonParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#relation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelation(OberonParser.RelationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#simpleExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleExpression(OberonParser.SimpleExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#addOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddOperator(OberonParser.AddOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm(OberonParser.TermContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#mulOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMulOperator(OberonParser.MulOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#factor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFactor(OberonParser.FactorContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#designator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDesignator(OberonParser.DesignatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#selector}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelector(OberonParser.SelectorContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#set_}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSet_(OberonParser.Set_Context ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElement(OberonParser.ElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#expList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpList(OberonParser.ExpListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#actualParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitActualParameters(OberonParser.ActualParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(OberonParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#assignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment(OberonParser.AssignmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#statementSequence}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatementSequence(OberonParser.StatementSequenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#ifStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfStatement(OberonParser.IfStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#whileStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileStatement(OberonParser.WhileStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#forStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForStatement(OberonParser.ForStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#declarationSequence}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclarationSequence(OberonParser.DeclarationSequenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#formalParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormalParameters(OberonParser.FormalParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#fPSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFPSection(OberonParser.FPSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#formalType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormalType(OberonParser.FormalTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OberonParser#module}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModule(OberonParser.ModuleContext ctx);
}