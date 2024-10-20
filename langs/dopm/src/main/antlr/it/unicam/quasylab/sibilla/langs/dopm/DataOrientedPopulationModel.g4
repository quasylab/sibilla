grammar DataOrientedPopulationModel;

@header {
package it.unicam.quasylab.sibilla.langs.dopm;
}

model   : element* EOF;

element : species_declaration
        | rule_declaration
        | system_declaration
        | measure_declaration
        | predicate_declaration;

species_declaration : 'species' name=ID (vars='{' var_decl (',' var_decl)* '}')? ';';

var_decl : name=ID ':' type=('integer'|'real'|'boolean');

system_declaration: 'system' name=ID '='  agents=system_composition ';' ;

system_composition :
    system_component ('|' system_component)*
    ;

system_component : agent_expression('#'INTEGER)?;

rule_declaration    :
    'rule' name=ID '{'
        body=rule_body
    '}'
    ;

rule_body : output=output_transition (broadcast='*')? '|>' inputs=input_transition_list;

agent_predicate:
    name=ID ('[' predicate=expr ']')?
    ;

output_transition :
        pre = agent_predicate
        '-[' rate=expr ']->'
        post = agent_mutation
    ;

input_transition :
        pre = agent_predicate
        '-[' sender_predicate=expr ':' probability=expr ']->'
        post = agent_mutation
    ;

input_transition_list :
    input = input_transition (',' input=input_transition)*
    ;

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

measure_declaration : 'measure' name=ID '=' expr ';';

predicate_declaration: 'predicate' name=ID '=' expr ';';

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
