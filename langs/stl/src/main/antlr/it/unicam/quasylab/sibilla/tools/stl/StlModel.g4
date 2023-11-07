grammar StlModel;

@header {
package it.unicam.quasylab.sibilla.langs.slam;
}

model   :
    declarationMeasure*
    declarationFormula*
    EOF
;

declarationMeasure: 'measure' name=ID ';';

declarationFormula: 'formula' name=ID ('[' (params+=formulaParameter (',' params+=formulaParameter)*)? ']')? ':'
    formula = stlFormula ';';

formulaParameter: name=ID '=' expr;

stlFormula:
        'true'                                                # stlFormulaTrue
        | 'false'                                             # stlFormulaFalse
        | '[' left=expr op=RELOP right=expr ']'               # stlFormulaAtomic
        | left=stlFormula ('&'|'&&') right=stlFormula         # stlFormulaAnd
        | left=stlFormula ('|'|'||') right=stlFormula         # stlFormulaOr
        | left=stlFormula '->' right=stlFormula               # stlFormulaImply
        | left=stlFormula '<->' right=stlFormula              # stlFormulaIfAndOnlyIf
        | '!' argument=stlFormula                             # stlFormulaNot
        | '(' stlFormula ')'                                  # stlFormulaBracket
        | left=stlFormula '\\U' interval right=stlFormula     # stlFormulaUntil
        | '\\E' interval arg=stlFormula                       # stlFormulaEventually
        | '\\G' interval arg=stlFormula                       # stlFomulaGlobally
        ;

interval: '[' from=expr ',' to=expr ']';


expr    :
      left=expr '^' right=expr                                   # expressionPow
    | left=expr op=('*'|'/'|'//') right=expr                     # expressionMulDiv
    | left=expr op=('+'|'-'|'%') right=expr                      # expressionAddSub
    | op=('-'|'+') arg=expr                                      # expressionUnaryOperator
    | '(' expr ')'                                               # expressionBracket
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
    ;




fragment DIGIT  :   [0-9];
fragment LETTER :   [a-zA-Z_];

ID              :   ('#'|'%')? LETTER (DIGIT|LETTER)*;
INTEGER         :   DIGIT+;
REAL            :   ((DIGIT* '.' DIGIT+)|DIGIT+ '.')(('E'|'e')('-')?DIGIT+)?;
RELOP           :   '<'|'<='|'=='|'!='|'>='|'>';

COMMENT
    : '/*' .*? '*/' -> channel(HIDDEN) // match anything between /* and */
    ;

WS  : [ \r\t\u000C\n]+ -> channel(HIDDEN)
    ;