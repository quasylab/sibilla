grammar YodaModel;

@header {
package it.unicam.quasylab.sibilla.langs.yoda;
}

model: element* EOF;

element : constant_declaration
        | parameter_declaration
        | agent_declaration;

//INITIALISATION PARAMETERS

constant_declaration: 'const' name=ID '=' expr ';';

parameter_declaration: 'param' name=ID '=' expr ';';

//AGENT GRAMMAR

agent_declaration: 'agent' name=ID '{'
    'state list: ' state_declaration ('|' state_declaration)* ','
    'observation list:' observation_declaration ('|' observation_declaration)* ','
    'action list:' action_declaration ('|' action_declaration)*','
    'behaviour:' behaviour_declaration
    '};'
    ;

state_declaration: 'state' name=ID '=' expr ';';

observation_declaration: 'observation' name=ID '=' expr ';';

action_declaration: 'action' name=ID '{'
    action_body('|' action_body)
    '};'
    ;

action_body: state_name=ID '=' expr;

behaviour_declaration: 'behaviour' name=ID '=' '{'
    (behaviour_rule)*
    def_behaviour_rule
    '};'
    ;

behaviour_rule:
    '['guardExpr=expr('|' guardExpr=expr)*']'
    '->'
    action_name=ID ':' times=expr ';';

def_behaviour_rule: 'default' action_name=ID ':' times=expr ';';

//ENVIRONMENT GRAMMAR

global_state_declaration: ; //TODO

internal_objects_declaration:; //TODO

agent_info_declaration: ;//TODO

ev_environment_declaration: ;//TODO

//SYSTEM CONFIG

assignment_declaration:; //TODO



system_declaration: 'system' name=ID '=' '{'

    '}'  ;//TODO



expr    : INTEGER                                                   # integerValue
        | REAL                                                      # realValue
        | 'false'                                                   # false
        | 'true'                                                    # true
        | reference=ID                                              # reference
        | '(' expr ')'                                              # brackets
        | leftOp=expr oper=('+'|'-') rightOp=expr                   # addsubOperation
        | leftOp=expr oper=('*'|'/') rightOp=expr                   # multdivOperation
        | leftOp=expr oper=('%'|'//') rightOp=expr                  # additionalOperation
        | leftOp=expr '^' rightOp=expr                              # exponentOperation
        | '!' argument=expr                                         # negation
        | leftOp=expr oper=('&'|'&&') rightOp=expr                  # andExpression
        | leftOp=expr oper=('|'|'||') rightOp=expr                  # orExpression
        | leftOp=expr oper=('<'|'<='|'=='|'>='|'>') rightOp=expr    # relationExpression
        | guardExpr=expr '?' thenBranch=expr ':' elseBranch=expr    # ifthenelseExpression
        ;



fragment DIGIT  :   [0-9];
fragment LETTER :   [a-zA-Z_];

ID              :   LETTER (DIGIT|LETTER)*;
INTEGER         :   DIGIT+;
REAL            :   ((DIGIT* '.' DIGIT+)|DIGIT+ '.')(('E'|'e')('-')?DIGIT+)?;
TYPE            :   ;

COMMENT         : '/*' .*? '*/' -> channel(HIDDEN); // match anything between /* and */
WS              : [ \r\t\u000C\n]+ -> channel(HIDDEN);