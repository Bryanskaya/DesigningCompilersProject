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
   : (assignment | ifStatement | whileStatement | forStatement)?
   ;

assignment
   : designator ':=' expression
   ;

statementSequence
   : statement (';' statement)*
   ;

ifStatement
   : IF expression THEN statementSequence (ELSIF expression THEN statementSequence)* (ELSE statementSequence)? END
   ;

whileStatement
   : WHILE expression DO statementSequence (ELSIF expression DO statementSequence)* END
   ;

forStatement
   : FOR ident ':=' expression TO expression (BY constExpression)? DO statementSequence END
   ;

declarationSequence
   : (CONST (constDeclaration ';')*)? (TYPE (typeDeclaration ';')*)? (VAR (variableDeclaration ';')*)?
   ;

module
   : MODULE ident ';' declarationSequence (BEGIN statementSequence)? RETURN factor ';' END ident '.' EOF
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

TO
   : 'TO'
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

