grammar PopulationModel;

@header {
package quasylab.sibilla.langs.pm;
}

model   : element*;

element : const_declaration | species_declaration | rule_declaration | param_declaration | measure_declaration | system_declaration ;

system_declaration: 'system' name=ID ('(' args+=ID (',' args+=ID)* ')')? '='  species_pattern;

const_declaration   : 'const' name=ID '=' expr ';';

species_declaration : 'species' name=ID ('of' range ('*' range)* )?;

range : '[' min=expr ',' max=expr ']';

rule_declaration    :
    'rule' name=ID '{'
        rulestatement
    '}'
    ;

rulestatement :
    for_statement | when_statement | rule_body
    ;

for_statement :
    'for' name=ID 'in' domain=range next=rulestatement
    ;

when_statement :
    'when' guard=expr arg=rulestatement
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
    species_expression ('[' size=expr ']')?
;


species_expression:
    name=ID ('<' expr (',' expr)* '>')?
    ;

measure_declaration : 'measure' name=ID '=' expr ';';

param_declaration   : 'param' name=ID ':' type 'default' expr ';';

type :
    'int' # intType
    | 'real' # realType
    ;

expr    :
      left=expr op=('<'|'<='|'=='|'>='|'>') right=expr          # relationExpression
    | left=expr op=('&'|'&&') right=expr                      # andExpression
    | left=expr op=('|'|'||') right=expr                      # orExpression
    | left=expr '^' right=expr                                # exponentExpression
    | left=expr op=('*'|'/'|'//') right=expr               # mulDivExpression
    | left=expr op=('+'|'-'|'%') right=expr                   # addSubExpression
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
    | 'now'                                        # nowExpression
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