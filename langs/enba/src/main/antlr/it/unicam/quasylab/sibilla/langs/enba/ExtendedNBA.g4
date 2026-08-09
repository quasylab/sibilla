grammar ExtendedNBA;

@header {
package it.unicam.quasylab.sibilla.langs.enba;
}

model   : element* EOF;

element : species_declaration
        | channel_declaration
        | process_declaration
        | system_declaration
        | measure_declaration
        | predicate_declaration;

species_declaration : 'species' name=ID (vars='{' var_decl (',' var_decl)* '}')? ';';

measure_declaration : 'measure' name=ID '=' expr ';';

predicate_declaration: 'predicate' name=ID '=' expr ';';

channel_declaration : 'channel' name=ID (vars='{'var_decl (',' var_decl)*'}')? ';';

process_declaration : 'process' name=ID '=' body=process_body ';';

process_body : choice_process
             | conditional_process
             | nil_process
             ;

conditional_process : '['predicate=expr']' then=process_body ':' else=process_body;

nil_process : '_NIL_';

choice_process : actions=action_tuple ('+' action_tuple)*;

action_tuple : input_tuple
             | output_tuple;

output_tuple : output_action '.' agent_mutation;
input_tuple : input_action '.' agent_mutation;

output_action : channel=ID (broadcast='*')? '!' '<' rate=expr '>' '[' predicate=expr ']';
input_action : channel=ID (broadcast='*')? '?' '<' probability=expr '>' '[' predicate=expr ']';



var_decl : name=ID ':' type=('integer'|'real'|'boolean');

system_declaration: 'system' name=ID '='  components=system_composition ';' ;

system_composition :
    system_component ('|' system_component)*
    ;

system_component : agent_expression('#'INTEGER)?;

agent_mutation :
    deterministic_mutation = agent_expression
    | '{' tuples = stochastic_mutation_tuple ('+' stochastic_mutation_tuple)* '}'
    ;

stochastic_mutation_tuple :
    '(' agent_expression ':' expr ')'
    ;

agent_expression:
    name=ID vars=var_ass_list
    ;

var_ass_list : '[' (var_ass)? (',' var_ass)* ']';
var_ass : name=ID '=' value=expr;

agent_predicate:
    name=ID ('[' predicate=expr ']')?
    ;

expr    :
      left=expr op=('&'|'&&') right=expr                      # andExpression
    | left=expr op=('|'|'||') right=expr                      # orExpression
    | left=expr '^' right=expr                                # exponentExpression
    | left=expr op=('*'|'/'|'//') right=expr               # mulDivExpression
    | left=expr op=('+'|'-'|'%') right=expr                   # addSubExpression
    |  left=expr op=('<'|'<='|'=='|'!='|'>='|'>') right=expr          # relationExpression
    | '!' arg=expr                                     # negationExpression
    | guard=expr '?' thenBranch=expr ':' elseBranch=expr             # ifThenElseExpression
    | op=('-'|'+') arg=expr                            # unaryExpression
    | 'abs(' expr ')'                                     # absExpression
    | '(' expr ')'                                     # bracketExpression
    | INTEGER                                      # intValue
    | REAL                                         # realValue
    | 'false'                                      # falseValue
    | 'true'                                       # trueValue
    | '%' agent=agent_predicate                                      # populationFractionExpression
    | '#' agent=agent_predicate                                       # populationSizeExpression
    | reference='sender.'ID                                 # senderReferenceExpression
    | reference='receiver.'ID                                 # receiverReferenceExpression
    | reference=ID                                 # referenceExpression
    ;

fragment DIGIT  :   [0-9];
fragment LETTER :   [a-zA-Z_];

ID              :   LETTER (DIGIT|LETTER)*;
INTEGER         :   DIGIT+;
REAL            :   ((DIGIT* '.' DIGIT+)|DIGIT+ '.')(('E'|'e')('-')?DIGIT+)?;


COMMENT
    : '/*' .*? '*/' -> channel(HIDDEN) // match anything between /* and */
    ;

WS  : [ \r\t\u000C\n]+ -> channel(HIDDEN)
    ;