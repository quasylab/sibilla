grammar PopulationModel;

@header {
package it.unicam.quasylab.sibilla.langs.pm;
}

model   : element* EOF;

element : const_declaration
        | species_declaration
        | rule_declaration
        | param_declaration
        | measure_declaration
        | system_declaration
        | label_declaration;

system_declaration: 'system' name=ID ('(' args+=ID (',' args+=ID)* ')')? '='  species_pattern ';' ;

const_declaration   : 'const' name=ID '=' expr ';';

species_declaration : 'species' name=ID ('of' range ('*' range)* )? ';';

range : '[' min=expr ',' max=expr ']';


label_declaration   : 'label' name=ID ('(' args+=ID (',' args+=ID)* ')')? '=' '{' species_expression (',' species_expression)* '}'
                    ;

//label_declaration   : 'label' name=ID ((local_variables) (guard_expression)?)? '=' '{' species_expression (',' species_expression)* '}'
//                    ;

rule_declaration    :
    'rule' name=ID ((local_variables) (guard_expression)?)? '{'
        body=rule_body
    '}'
    ;

rule_body :
        ('[' guard=expr ']')?
        pre=species_pattern
        '-[' rate=expr ']->'
        post = species_pattern
    ;

species_pattern :
    species_pattern_element ('|' species_pattern_element)*
    ;

species_pattern_element:
    species_expression ( '<' size=expr '>')?
;


species_expression:
    name=ID ('[' expr (',' expr)* (local_variables)? (guard_expression)? ']')?
    ;

local_variables: 'for' variables += local_variable ('and' variables += local_variable)*;

local_variable: name=ID 'in' range;

guard_expression: 'when' guard=expr;

measure_declaration : 'measure' name=ID ((local_variables) (guard_expression)?)? '=' expr ';';

param_declaration   : 'param' name=ID '=' expr ';';

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
    | '%' agent=species_expression                                       # populationFractionExpression
    | '#' agent=species_expression                                       # populationSizeExpression
//    | 'now'                                        # nowExpression
    | reference=ID                                 # referenceExpression
//    | 'abs' '(' expr ')'
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