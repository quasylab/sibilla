grammar YodaModel;

@header {
package it.unicam.quasylab.sibilla.langs.yoda;
}

model: element* EOF;

element : constant_declaration
        | parameter_declaration
        | agent_declaration;

constant_declaration: 'const' name=ID '=' expr ';';

parameter_declaration: 'param' name=ID '=' expr ';';

//AGENT GRAMMAR

agent_declaration: 'agent' name=ID '{'
    'state list: ' state_declaration ('|' state_declaration)* ','
    'observation list:' observation_declaration ('|' observation_declaration)* ','
    //TODO
    '}'';'
    ;

state_declaration: 'state' name=ID '=' expr ';';

observation_declaration: 'observation' name=ID '=' expr ';';

action_declaration: 'action' name=ID '{'
    //TODO
    '}'
    ;

set_action_declaration: ;//TODO forse manco serve

behaviour_declaration: ; //TODO

//ENVIRONMENT GRAMMAR

system_declaration: 'system' name=ID '='   ;//TODO



expr    : INTEGER
        | REAL
        | 'false'
        | 'true';



fragment DIGIT  :   [0-9];
fragment LETTER :   [a-zA-Z_];

ID              :   LETTER (DIGIT|LETTER)*;
INTEGER         :   DIGIT+;
REAL            :   ((DIGIT* '.' DIGIT+)|DIGIT+ '.')(('E'|'e')('-')?DIGIT+)?;
TYPE            :   ;

COMMENT         : '/*' .*? '*/' -> channel(HIDDEN); // match anything between /* and */