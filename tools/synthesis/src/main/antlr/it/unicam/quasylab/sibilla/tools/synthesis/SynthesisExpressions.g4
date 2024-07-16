grammar SynthesisExpressions;

@header {
    package it.unicam.quasylab.sibilla.tools.synthesis;
}

expression   : expr | EOF ;

//expressionDeclaration : expr;


expr    :
      '(' expr ')'                                               # expressionBracket
    | op=('-'|'+') arg=expr                                      # expressionUnaryOperator
    | <assoc=right> left=expr '^' right=expr                     # expressionPow
    | left=expr op=('*'|'/'|'//') right=expr                     # mulDivExpression
    | left=expr op=('+'|'-'|'%') right=expr                      # addSubExpression
    | left=expr op=('<'|'<='|'=='|'!='|'>='|'>') right=expr      # relationExpression
    | '!''(' argument=expr ')'                                   # negationExpression
    | left=expr op=('&'|'&&') right=expr                         # andExpression
    | left=expr op=('|'|'||') right=expr                         # orExpression
    | guard=expr '?' thenBranch=expr ':' elseBranch=expr         # ifThenElseExpression
    | 'true'                                                     # expressionTrue
    | 'false'                                                    # expressionFalse
    | INTEGER                                                    # expressionInteger
    | REAL                                                       # expressionReal
    | reference=ID                                               # expressionReference
    | 'abs' '(' argument=expr ')'                                # expressionAbs
    | 'acos' '(' argument=expr ')'                               # expressionACos
    | 'asin' '(' argument=expr ')'                               # expressionASin
    | 'atan' '(' argument=expr ')'                               # expressionATan
    | 'ceil' '(' argument=expr ')'                               # expressionCeil
    | 'cos' '(' argument=expr ')'                                # expressionCos
    | 'cosh' '(' argument=expr ')'                               # expressionCosh
    | 'exp' '(' argument=expr ')'                                # expressionExp
    | 'sqrt' '(' argument=expr ')'                               # expressionSqrt
    | 'floor' '(' argument=expr ')'                              # expressionFloor
    | 'log10' '(' argument=expr ')'                              # expressionLog10
    | 'log' '(' argument=expr ')'                                # expressionLog
    | 'sin' '(' argument=expr ')'                                # expressionSin
    | 'sinh' '(' argument=expr ')'                               # expressionSinh
    | 'tan' '(' argument=expr ')'                                # expressionTan
    | 'tanh' '(' argument=expr ')'                               # expressionTanh
    | 'atan2' '(' firstArgument=expr ',' secondArgument=expr ')' # expressionATan2
    | 'max' '(' firstArgument=expr ',' secondArgument=expr ')'   # expressionMax
    | 'min' '(' firstArgument=expr ',' secondArgument=expr ')'   # expressionMin
    | 'PI'                                                       # expressionPi
    | 'E'                                                        # expressionE
    | ('negInf'|'posInf')                                        # expressionInfinity
    ;


fragment DIGIT  :   [0-9];
fragment LETTER :   [a-zA-Z_];

ID              :   ('#'|'%')? LETTER (DIGIT|LETTER)*;
INTEGER         :   DIGIT+;
REAL            :   ((DIGIT* '.' DIGIT+)|DIGIT+ '.')(('E'|'e')('-')?DIGIT+)?;

COMMENT
    : '/*' .*? '*/' -> channel(HIDDEN) // match anything between /* and */
    ;

WS  : [ \r\t\u000C\n]+ -> channel(HIDDEN)
    ;