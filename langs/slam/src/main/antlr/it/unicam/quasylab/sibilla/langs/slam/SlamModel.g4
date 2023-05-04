grammar SlamModel;

@header {
package it.unicam.quasylab.sibilla.langs.slam;
}

model   :
    (elements += modelElement)*
    EOF
;

modelElement :
    declarationParameter
    | declarationConstant
    | declarationMessage
    | declarationAgent
    | declarationMeasure
    | declarationPredicate
    | declarationSystem;

declarationPredicate:
    'predicate' name=ID '=' value=expr ';'
;

declarationConstant:
    'const' name=ID '=' value=expr ';'

;


declarationParameter:
    'param' name=ID '=' value=expr ';'
;



declarationMeasure: 'measure' name=ID '=' expr;

declarationSystem:
    'system' name=ID '(' (params += agentParameter (',' params += agentParameter)*)? ')' '='
        agents += agentExpression ('|' agents += agentExpression)* ';'

;


agentExpression:
    name=ID '(' (args += expr (','  args += expr)*)? ')' ('#' copies=expr)
;


declarationMessage:
    'message' tag=ID ('of' content+=slamType ('*' content+=slamType)* )? ';'
    ;

declarationAgent:
    'agent' name=ID ('(' (params += agentParameter (',' params += agentParameter)*)? ')')? '{'
        'attributes' '{'
            (attributes += attributeDeclaration)*
        '}'
        ('views' '{'
            (views += attributeDeclaration)*
        '}')?
        'behaviour' '{'
            (states += agentStateDeclaration)*
        '}'
        ('on' 'time'  '{'
            (commands += assignmentCommand)*
        '}')?
    '}'
    ;

agentParameter:
    type=slamType name=ID
;

slamType:
      'int'                                 #intType
    | 'real'                                #realType
    | 'boolean'                             #booleanType
//    | 'list' '<' contentType=slamType '>'  #booleanType
    ;

attributeDeclaration:
        name=ID '=' expr ';'
    ;

agentStateDeclaration:
    (isInit='init')? 'state' name=ID '{'
        (handlers += stateMessageHandler)*
        ('after' sojournTimeExpression=expr activityBlock)
        ('on' 'time' '{'
            (commands += assignmentCommand)*
        '}')?
    '}'
;

stateMessageHandler:
    'on receive' tag=ID '[' (content += valuePattern (',' content+=valuePattern)*) ']' ('from' agentGuard=agentPattern)? ('when' guard=expr)? activityBlock
;

valuePattern:
    '_'                     # patternAnyValue
    | '?' name=ID           # patternVariable
;

activityBlock:
    nextStateBlock
    | probabilitySelectionBlock
    | skipBlock
;

probabilitySelectionBlock: 'select' '{'
    (cases += selectCase)*
'}'
;

selectCase:
    '[' weight=expr ']' ('when' guard=expr)? nextStateBlock
;

skipBlock:
    'none'
;

nextStateBlock:
    '->' name=ID (block=agentCommandBlock|';')
;

agentCommandBlock:
    '{'
        (commands += agentCommand)*
    '}'
;

agentCommand:
    ifThenElseCommand
    | sendCommand
    | assignmentCommand
    | spawnCommand
;

ifThenElseCommand:
    'if' guard=expr thenCommand=agentCommand ('else' elseCommand=agentCommand)?
;

sendCommand:
    'send' content=messageExpression ('to' target=agentPattern)? 'in' time=expr ';'
;

assignmentCommand:
    name=ID '=' expr ';'
;

spawnCommand:
    'spawn' agent=agentExpression 'in' time=expr ';'
;


agentPattern:
    '_'                   # agentPatternAny
    | name=ID '[' guard = expr  ']'                 # agentPatternNamed
    | ('_')? '[' guard=expr ']'                            # agentPatternProperty
    | left=agentPattern '|' right=agentPattern     # agentPatternDisjunction
    | left=agentPattern '&' right=agentPattern     # agentPatternConjunction
    | '!' arg=agentPattern                         # agentPatternNegation
    | '(' agentPattern ')'                         # agentPatternBrackets
;


messageExpression:
    tag=ID ('[' (elements += expr (',' elements += expr)*)? ']')?
;

/*
element : const_declaration
        | species_declaration
        | rule_declaration
        | param_declaration
        | measure_declaration
        | system_declaration
        | label_declaration
        | predicate_declaration;

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

predicate_declaration: 'predicate' name=ID '=' expr ';';

param_declaration   : 'param' name=ID '=' expr ';';

*/
expr    :
      left=expr op=('&'|'&&') right=expr                      # expressionAnd
    | left=expr op=('|'|'||') right=expr                      # expressionOr
    | left=expr '^' right=expr                                # expressionPow
    | left=expr op=('*'|'/'|'//') right=expr               # expressionMulDiv
    | left=expr op=('+'|'-'|'%') right=expr                   # expressionAddSub
    | left=expr op=RELOP right=expr                    # expressionRelation
//    | left=expr '<@' right=expr                        # expressionAddToHead
//    | left=expr '@>' right=expr                        # expressionAddToTail
//    | expr '[' index=expr ']'                          # expressionIndexedValue
    | 'U' '[' from=expr ',' to=expr ']'                # expressionSamplingUniform
    | 'N' '[' mean=expr ',' sigma=expr ']'             # expressionSamplingNormal
    | 'rnd'                                            # expressionRandomValue
    | 'exists''{' agentPattern '}'                     # expressionExistsAgent
    | 'forall' '{' agentPattern '}'                     # expressionForAllAgents
    | 'sum' '{' expr ':' agentPattern '}'               # expressionSumAgents
    | 'min' '{' expr ':' agentPattern '}'               # expressionMinAgents
    | 'max' '{' expr ':' agentPattern '}'               # expressionMaxAgents
    | 'mean' '{' expr ':' agentPattern '}'               # expressionMeanAgents
    | '!' arg=expr                                      # expressionNegation
    | guard=expr '?' thenBranch=expr ':' elseBranch=expr             # expressionIfThenElse
    | op=('-'|'+') arg=expr                            # expressionUnaryOperator
    | '(' expr ')'                                     # expressionBracket
    | '(' type=slamType ')' expr                   # expressionCast
    | INTEGER                                      # expressionInteger
    | REAL                                         # expressionReal
    | 'false'                                      # expressionFalse
    | 'true'                                       # expressionTrue
    | 'now'                                        # expressionNow
    | 'dt'                                         # expressionDt
    | ('this' '.') reference=ID                                 # expressionReference
    | 'abs' '(' argument=expr ')'                             # expressionAbs
//    | 'head' '(' argument=expr ')'                          # expressionHead
//    | 'tail' '(' argument=expr ')'                          # expressionTail
    | 'acos' '(' argument=expr ')'                          # expressionACos
    | 'asin' '(' argument=expr ')'                          # expressionASin
    | 'atan' '(' argument=expr ')'                          # expressionATan
    | 'ceil' '(' argument=expr ')'                          # expressionCeil
    | 'cos' '(' argument=expr ')'                          # expressionCos
    | 'cosh' '(' argument=expr ')'                          # expressionCosh
    | 'exp' '(' argument=expr ')'                          # expressionExp
    | 'floor' '(' argument=expr ')'                          # expressionFloor
    | 'log10' '(' argument=expr ')'                          # expressionLog10
    | 'log' '(' argument=expr ')'                          # expressionLog
    | 'sin' '(' argument=expr ')'                          # expressionSin
    | 'sinh' '(' argument=expr ')'                          # expressionSinh
    | 'tan' '(' argument=expr ')'                          # expressionTan
    | 'tanh' '(' argument=expr ')'                          # expressionTanh
//    | 'length' '(' argument=expr ')'                          # expressionLength
    | 'atan2' '(' firstArgument=expr ',' secondArgument=expr ')' # expressionATan2
    | 'max' '(' firstArgument=expr ',' secondArgument=expr ')' # expressionMax
    | 'min' '(' firstArgument=expr ',' secondArgument=expr ')' # expressionMin
//    | 'abs' '(' expr ')'
    ;





fragment DIGIT  :   [0-9];
fragment LETTER :   [a-zA-Z_];

ID              :   LETTER (DIGIT|LETTER)*;
INTEGER         :   DIGIT+;
REAL            :   ((DIGIT* '.' DIGIT+)|DIGIT+ '.')(('E'|'e')('-')?DIGIT+)?;
RELOP           :   '<'|'<='|'=='|'!='|'>='|'>';

COMMENT
    : '/*' .*? '*/' -> channel(HIDDEN) // match anything between /* and */
    ;

WS  : [ \r\t\u000C\n]+ -> channel(HIDDEN)
    ;