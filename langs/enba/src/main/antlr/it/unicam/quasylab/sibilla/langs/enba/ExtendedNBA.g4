grammar ExtendedNBA;

@header {
package it.unicam.quasylab.sibilla.langs.enba;
}

model   : element* EOF;

element : process_declaration
        | channel_declaration
        | system_declaration
        | measure_declaration
        | predicate_declaration;

measure_declaration : 'measure' name=ID '=' expr ';';

predicate_declaration: 'predicate' name=ID '=' expr ';';

channel_declaration : 'channel' name=ID ';';

process_declaration : 'process' name=ID (vars='('var_decl (',' var_decl)*')')? '{'
    body=process_body
'}';

process_body : choice_process
             | conditional_process
             | nil_process
             ;

choice_process : actions=action '.' process_mutation ('+' action '.' process_mutation)*;

action : broadcast_output_action
       | broadcast_input_action
       ;

broadcast_output_action : channel=ID '*' '<' rate=expr '>' '[' predicate=expr ']' '!';
broadcast_input_action : channel=ID '*' '<' probability=expr '>' '[' predicate=expr ']' '?';

conditional_process : '['predicate=expr']' then=process_body ':' else=process_body;

nil_process : '_';

var_decl : name=ID ':' type=('integer'|'real'|'boolean');

system_declaration: 'system' name=ID '='  processs=system_composition ';' ;

system_composition :
    process_instantation ('|' process_instantation)*
    ;

process_instantation : process_expression('#'INTEGER)?;

process_mutation :
    deterministic_mutation = process_expression
    | '{' tuples = stochastic_mutation_tuple ('+' stochastic_mutation_tuple)* '}'
    ;

stochastic_mutation_tuple :
    '(' process_expression ':' expr ')'
    ;

process_expression:
    name=ID vars=var_ass_list
    ;

var_ass_list : '[' (var_ass)? (',' var_ass)* ']';
var_ass : name=ID '=' value=expr;

process_predicate:
    name=ID ('[' predicate=expr ']')?
    ;

expr    :
      left=expr op=('&'|'&&') right=expr                      # andExpression
    | left=expr op=('|'|'||') right=expr                      # orExpression
    | left=expr '^' right=expr                                # exponentExpression
    | left=expr op=('*'|'/'|'//') right=expr               # mulDivExpression
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
    | '%' process=process_predicate                                      # populationFractionExpression
    | '#' process=process_predicate                                       # populationSizeExpression
    | reference='sender.'ID                                 # senderReferenceExpression
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