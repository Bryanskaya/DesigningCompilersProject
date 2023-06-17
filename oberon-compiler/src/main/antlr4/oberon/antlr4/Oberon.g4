grammar Oberon;



ident
   : IDENT
   ;

qualident
   : ident
   ;

identdef
   : ident '*'?
   ;

integer
   : (DIGIT+)
   ;

real
   : DIGIT+ '.' DIGIT*
   ;

number
   : integer
   | real
   ;

constDeclaration
   : identdef '=' constExpression
   ;

constExpression
   : expression
   ;

typeDeclaration
   : identdef '=' type_
   ;

type_
   : qualident
   | arrayType
   | pointerType
   | procedureType
   ;

arrayType
   : ARRAY length OF type_
   ;

length
   : constExpression
   ;

identList
   : identdef (',' identdef)*
   ;

pointerType
   : POINTER TO type_
   ;

procedureType
   : PROCEDURE formalParameters?
   ;

variableDeclaration
   : identList ':' type_
   ;

expression
   : simpleExpression (relation simpleExpression)?
   ;

relation
   : '='
   | '#'
   | '<'
   | '<='
   | '>'
   | '>='
   | IN
   | IS
   ;

simpleExpression
   : ('+' | '-')? term (addOperator term)*
   ;

addOperator
   : '+'
   | '-'
   | OR
   ;

term
   : factor (mulOperator factor)*
   ;

mulOperator
   : '*'
   | '/'
   | DIV
   | MOD
   | '&'
   ;

factor
   : number
   | STRING
   | NIL
   | TRUE
   | FALSE
   | set_
   | designator (actualParameters)?
   | '(' expression ')'
   | '~' factor
   ;

designator
   : qualident selector*
   ;

selector
   : '[' expList ']'
   ;

set_
   : '{' (element (',' element)*)? '}'
   ;

element
   : expression ('..' expression)?
   ;

expList
   : expression (',' expression)*
   ;

actualParameters
   : '(' expList? ')'
   ;

statement
   : (assignment | procedureCall | ifStatement | caseStatement | whileStatement | forStatement)?
   ;

assignment
   : designator ':=' expression
   ;

procedureCall
   : designator actualParameters?
   ;

statementSequence
   : statement (';' statement)*
   ;

ifStatement
   : IF expression THEN statementSequence (ELSIF expression THEN statementSequence)* (ELSE statementSequence)? END
   ;

caseStatement
   : CASE expression OF case_ ('|' case_)* END
   ;

case_
   : (caseLabelList ':' statementSequence)?
   ;

caseLabelList
   : labelRange (',' labelRange)*
   ;

labelRange
   : label ('..' label)?
   ;

label
   : integer
   | STRING
   | qualident
   ;

whileStatement
   : WHILE expression DO statementSequence (ELSIF expression DO statementSequence)* END
   ;

forStatement
   : FOR ident ':=' expression TO expression (BY constExpression)? DO statementSequence END
   ;

procedureDeclaration
   : procedureHeading ';' procedureBody ident
   ;

procedureHeading
   : PROCEDURE identdef formalParameters?
   ;

procedureBody
   : declarationSequence (BEGIN statementSequence)? (RETURN expression)? END
   ;

declarationSequence
   : (CONST (constDeclaration ';')*)? (TYPE (typeDeclaration ';')*)? (VAR (variableDeclaration ';')*)? (procedureDeclaration ';')*
   ;

formalParameters
   : '(' (fPSection (';' fPSection)*)? ')' (':' qualident)?
   ;

fPSection
   : VAR? ident (',' ident)* ':' formalType
   ;

formalType
   : (ARRAY OF)* qualident
   ;

module
   : MODULE ident ';' importList? declarationSequence (BEGIN statementSequence)? RETURN factor ';' END ident '.' EOF
   ;

importList
   : IMPORT import_ (',' import_)* ';'
   ;

import_
   : ident (':=' ident)?
   ;

ARRAY
   : 'ARRAY'
   ;

OF
   : 'OF'
   ;

END
   : 'END'
   ;

POINTER
   : 'POINTER'
   ;

TO
   : 'TO'
   ;

PROCEDURE
   : 'PROCEDURE'
   ;

IN
   : 'IN'
   ;

IS
   : 'IS'
   ;

OR
   : 'OR'
   ;

DIV
   : 'DIV'
   ;

MOD
   : 'MOD'
   ;

NIL
   : 'NIL'
   ;

TRUE
   : 'TRUE'
   ;

FALSE
   : 'FALSE'
   ;

IF
   : 'IF'
   ;

THEN
   : 'THEN'
   ;

ELSIF
   : 'ELSIF'
   ;

ELSE
   : 'ELSE'
   ;

CASE
   : 'CASE'
   ;

WHILE
   : 'WHILE'
   ;

DO
   : 'DO'
   ;

FOR
   : 'FOR'
   ;

BY
   : 'BY'
   ;

BEGIN
   : 'BEGIN'
   ;

RETURN
   : 'RETURN'
   ;

CONST
   : 'CONST'
   ;

TYPE
   : 'TYPE'
   ;

VAR
   : 'VAR'
   ;

MODULE
   : 'MODULE'
   ;

IMPORT
   : 'IMPORT'
   ;

STRING
   : ('"' .*? '"')
   ;

IDENT
   : LETTER (LETTER | DIGIT)*
   ;

LETTER
   : [a-zA-Z]
   ;

DIGIT
   : [0-9]
   ;

COMMENT
   : '(*' .*? '*)' -> skip
   ;

WS
   : [ \t\r\n] -> skip
   ;

