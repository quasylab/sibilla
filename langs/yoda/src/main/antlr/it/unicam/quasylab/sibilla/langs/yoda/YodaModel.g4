grammar YodaModel;

@header {
package it.unicam.quasylab.sibilla.langs.yoda;
}

model: element* EOF;

element : constant_declaration
        | parameter_declaration
        | agent_declaration
        | world_declaration
        | system_declaration;

//INITIALISATION PARAMETERS

constant_declaration: 'const' name=ID '=' expr ';';

parameter_declaration: 'param' name=ID '=' expr ';';

//AGENT GRAMMAR

agent_declaration: 'agent' name=ID '{'
    'state list:' state_declaration ('|' state_declaration)* ','
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

//WORLD GRAMMAR

world_declaration: 'name' name=ID '{'
    'global:' global_state_declaration
    'sensing:' sensing_declaration
    'environment' ev_environment_declaration
    '};'
    ;

global_state_declaration: '{'
    internal_objects_declaration
    '|'
    agent_info_declaration
    '};'
    ;

agent_info_declaration: 'agent' name=ID '{'
    name_var=ID ';' (name_var=ID';')*
    '};' ;

internal_objects_declaration: 'object' object_name=ID; //TODO

sensing_declaration:'sensing''{'
    (agent_sensing)*
    '}';

agent_sensing: name=ID'{'
    (sensing_name=ID '=' gexpr)*
    '}' ;

ev_environment_declaration: 'environment' name=ID '=' '{'
    //(environment_rule)?
    def_environment_rule
    '};'

    ;//TODO

environment_rule:;//TODO

def_environment_rule:'default' env_rule=ID ':' times=expr ';';

//SYSTEM CONFIG

assignment_declaration:
    'let' name=ID '=' expr
    ('and' name=ID '=' expr)*
    'in'
    ;

collective_declaration:
    '{'collective_body'}'; //TODO

collective_body:
               | 'for' name=ID 'in' group_name=ID '{'collective_body'}'
               | 'if' expr_bool=expr '{'collective_body'}'('if' expr_bool=expr '{'collective_body'}')* ('else''{'collective_body'}')?
               ;//TODO

system_declaration: 'system' name=ID '=' '{'
    (assignment_declaration)?
    collective_declaration
    '}';

//UTIL

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

gexpr   : expr                                                      # expression
        | parent=ID '.' son=ID                                      # attributeRef
        | 'forall'                                                  # forall //TODO
        | 'exists'                                                  # existsExpr //TODO
        | 'min'                                                     # minimumExpr //TODO
        | 'max'                                                     # maximumExpr //TODO
        | 'it.' ID                                                  # itself
        ;


fragment DIGIT  :   [0-9];
fragment LETTER :   [a-zA-Z_];

ID              :   LETTER (DIGIT|LETTER)*;
INTEGER         :   DIGIT+;
REAL            :   ((DIGIT* '.' DIGIT+)|DIGIT+ '.')(('E'|'e')('-')?DIGIT+)?;
TYPE            :   ;

COMMENT         : '/*' .*? '*/' -> channel(HIDDEN); // match anything between /* and */
WS              : [ \r\t\u000C\n]+ -> channel(HIDDEN);