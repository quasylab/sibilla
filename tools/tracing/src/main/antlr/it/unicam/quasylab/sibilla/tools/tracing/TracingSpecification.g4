grammar TracingSpecification;

@header {
package it.unicam.quasylab.sibilla.tools.tracing;
}

tracing   :
    (assignments += fieldAssignments)*
    EOF
;

fieldAssignments:
    name=ID '=' value=expr ';'                  # simpleFieldAssignment
   | name=ID '=' block=whenBlock                # blockFieldAssignment
;

whenBlock: '{'
    ('when' guards += expr ':' values+=expr ';')*
    'otherwise' default=expr ';'
'}'
;

expr    :
      left=expr '^' right=expr                                   # expressionPow
    | left=expr op=('*'|'/'|'//') right=expr                     # expressionMulDiv
    | left=expr op=('+'|'-'|'%') right=expr                      # expressionAddSub
    | op=('-'|'+') arg=expr                                      # expressionUnaryOperator
    | '(' expr ')'                                               # expressionBracket
    | INTEGER                                                    # expressionInteger
    | REAL                                                       # expressionReal
    | reference=ID                                               # expressionReference
    | 'it' '.' reference=ID                                      # expressionItReferemce
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
    | 'PI'                                                       # expressionPi
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