grammar MarkovChainModel;

@header {
package it.unicam.quasylab.sibilla.langs.markov;
}

model   : (model_type)? element* state_declaration rules_declaration configuration_declaration (measure_declaration)*;

configuration_declaration:
    single_declaration | multiple_declarations
    ;

single_declaration:
    'init' '(' variables? ')' '=' assignments
    ;

variables:
    vars+=ID (',' vars+=ID)*
;

multiple_declarations: 'init' '{' init_declarations+ '}' ;

init_declarations:
    (defaultToken = 'default')? name=ID '(' variables? ')' '=' assignments
    ;

assignments:
    '[' variable_assignment (',' variable_assignment)* ']'
;

variable_assignment: name=ID '=' value=expr;

model_type:
        'ctmc'          # ctmcModel
        | 'dtmc'        # dtmcModel
        ;

state_declaration: 'state' '{' variable_declaration+ '}'
        ;

variable_declaration:
            '[' min=expr '..' max=expr ']' name = ID ';'
        ;


element : const_declaration
        | param_declaration
        ;

const_declaration   : 'const' name=ID '=' expr ';';

rules_declaration    :
    'rules' '{'
        rule_case*
    '}'
    ;

rule_case: '[]' (guard=expr) '->' (step ('+' step)*) ';'
    ;

step: (weight=expr ':')? updates;

updates :
    '_' # emptyUpdate
    | variable_update ('&' variable_update)* # listUpdate
    ;

variable_update :
   '(' target=NEXT_ID '=' value=expr ')'
    ;

measure_declaration : 'measure' name=ID '=' expr ';';

param_declaration   : 'param' name=ID '=' value=REAL ';';

expr    :
      left=expr op=('&'|'&&') right=expr                      # andExpression
    | left=expr op=('|'|'||') right=expr                      # orExpression
    | left=expr '^' right=expr                                # exponentExpression
    | left=expr op=('*'|'/') right=expr               # mulDivExpression
    | left=expr op=('+'|'-'|'%') right=expr                   # addSubExpression
    |  left=expr op=('<'|'<='|'=='|'>='|'>') right=expr          # relationExpression
    | '!' arg=expr                                     # negationExpression
    | guard=expr '?' thenBranch=expr ':' elseBranch=expr             # ifThenElseExpression
    | op=('-'|'+') arg=expr                            # unaryExpression
    | '(' expr ')'                                     # bracketExpression
    | INTEGER                                      # intValue
    | REAL                                         # realValue
    | 'false'                                      # falseValue
    | 'true'                                       # trueValue
    | 'int' '(' arg = expr ')'                     # castToIntExpression
//    | 'now'                                        # nowExpression
    | reference=ID                                 # referenceExpression
//    | 'abs' '(' expr ')'
    ;





fragment DIGIT  :   [0-9];
fragment LETTER :   [a-zA-Z_];

ID              :   LETTER (DIGIT|LETTER)*;
INTEGER         :   DIGIT+;
REAL            :   ((DIGIT* '.' DIGIT+)|DIGIT+ '.')(('E'|'e')('-')?DIGIT+)?;
NEXT_ID         :   LETTER (DIGIT|LETTER)* '\'';

COMMENT
    : '/*' .*? '*/' -> channel(HIDDEN) // match anything between /* and */
    ;

WS  : [ \r\t\u000C\n]+ -> channel(HIDDEN)
    ;