grammar YodaModel;

@header {
package it.unicam.quasylab.sibilla.langs.yoda;
}

model: element* EOF;



element : constantDeclaration
        | parameterDeclaration
        | typeDeclaration
        | agentDeclaration
        | systemDeclaration
        | configurationDeclaration;

//PARAMETERS

constantDeclaration: 'const' name = ID '=' value=expr ';';

parameterDeclaration: 'param' name=ID '=' value=expr ';';

typeDeclaration:
    'type' name=ID '{'
        (type varName=ID ';')+
    '}';


//AGENT GRAMMAR

agentDeclaration:
    'agent' agentName=ID '{'
        'state' '{' knowledgeDeclaration informationDeclaration '}'
        'observations' '{' observationDeclaration '}'
        'actions' '{' actionBody ('|' actionBody)*'}'
        'behaviour' '{'behaviourDeclaration'}'
    '}';

knowledgeDeclaration: 'knowledge' '{' (newField)* '}';

informationDeclaration: 'information' '{' (newField)* '}';

observationDeclaration: (newField)* ;

newField: type fieldName=ID ';' ;

actionBody: actionName=ID '{' (fieldUpdate)+ '}'
          | 'wait'    //the agent waits a turn
          | 'stop'    //the agent stops
          | 'rtf'     //the agent return to the Force
;

fieldUpdate: fieldName=ID '<-' value=expr ';';

behaviourDeclaration: (ruleDeclaration '|' )* defaultRule;

ruleDeclaration: '[' boolExpr=expr ']' '->' '{' (weightedRule)+ '}';

defaultRule: 'default' '{' (weightedRule)+ '}';

weightedRule: actionName=ID ':' weight=expr ';';

//SYSTEM GRAMMAR

systemDeclaration:
    'system' name=ID '{'
        'scene' '{' (newField)* '}'
        'sensing' '{' (assignmentTemp)? (agentSensing)* '}'
        'evolution' '{' (evolutionDeclaration)* '}'
    '}'
;

assignmentTemp: 'let' tempName=ID '=' selectionBlock ('and' selectionBlock)*;

selectionBlock: 'select' agentName=ID 'in' agentTypeName=ID 'that' expr;

agentSensing: agentName=ID '{' (fieldUpdate)* '}';

evolutionDeclaration: entityName=ID '{' (fieldUpdate)* '}';

//CONFIG GRAMMAR

configurationDeclaration: 'configuration' name=ID '{'
    assignmentDeclaration
    '{'(collectiveDeclaration)*'}'
    '}';

assignmentDeclaration:
    'let' name=ID '=' func
    ('and' name=ID '=' func)*
    'in' systemName=ID
    ;

collectiveDeclaration: collectionName=ID '{' (fieldInit)* '}'
                     | 'for' name=ID 'in' groupName=ID '{' collectiveDeclaration '}'
                     | ('if' expr_bool=expr '{'collectiveDeclaration'}')+
                       ('else''{'collectiveDeclaration'}')?
;

fieldInit: fieldName=ID '=' value=expr;

//UTIL

expr    : INTEGER                                                   # integerValue
        | REAL                                                      # realValue
        | 'false'                                                   # false
        | 'true'                                                    # true
        | reference=ID                                              # reference
        | '(' expr ')'                                              # exprBrackets
      //  | gexpr                                                     # gexprCall
        | oper=('+'|'-') arg=expr                                   # unaryExpression
        | leftOp=expr oper=('+'|'-') rightOp=expr                   # addsubOperation
        | leftOp=expr oper=('*'|'/') rightOp=expr                   # multdivOperation
        | leftOp=expr oper=('%'|'//') rightOp=expr                  # additionalOperation
        | leftOp=expr '^' rightOp=expr                              # exponentOperation
        | '!' argument=expr                                         # negationExpression
        | 'sqrt' '(' argument=expr ')'                              # squareRootExpression
        | leftOp=expr oper=('&'|'&&') rightOp=expr                  # andExpression
        | leftOp=expr oper=('|'|'||') rightOp=expr                  # orExpression
        | leftOp=expr oper=('<'|'<='|'=='|'>='|'>') rightOp=expr    # relationExpression
        | guardExpr=expr '?' thenBranch=expr ':' elseBranch=expr    # ifthenelseExpression
      //  | '[' fieldAssignment (',' fieldAssignment)* ']'            # recordExpression
        | 'U''['min=expr',' max=expr']'                             # weightedRandomExpression
        | 'rnd'                                                     # randomExpression
        | parent=ID '.' son=ID                                      # attributeRef
        | 'forall' expr ('for' name=ID 'in' groupName=ID)?          # forallExpression
        | 'exists' expr ('for' name=ID 'in' groupName=ID)?          # existsExpression
        | 'min'    expr ('for' name=ID 'in' groupName=ID)?          # minimumExpression
        | 'max'    expr ('for' name=ID 'in' groupName=ID)?          # maximumExpression
        | 'it.' ID                                                  # itselfRef
;

type    : 'int'                                                     # integerNumber
        | 'real'                                                    # realNumber
        | 'bool'                                                    # boolean
        | 'char'                                                    # character
        | 'String'                                                  # string
        | 'array[' type (',' type)* ']'                             # arrayMultipleTypes
        | typeDeclaration                                          # newType
;

func    : 'generate' '('
            '[' name_value=ID '=' expr (',' name_value=ID '=' expr)* ']'
            ')'
        | 'distinct' '('
            init_number=expr ',' '[' name_value=ID '=' expr (',' name_value=ID '=' expr)* ']'
            ')'
        | 'distinctFrom''('
            init_number=expr ',' '[' name_value=ID '=' expr (',' name_value=ID '=' expr)* ']' ','  name=ID
            ')'
        ;

fragment DIGIT  :   [0-9];
fragment LETTER :   [a-zA-Z_];

ID              :   LETTER (DIGIT|LETTER)*;
INTEGER         :   DIGIT+;
REAL            :   ((DIGIT* '.' DIGIT+)|DIGIT+ '.')(('E'|'e')('-')?DIGIT+)?;

COMMENT         : '/*' .*? '*/' -> channel(HIDDEN); // match anything between /* and */
WS              : [ \r\t\u000C\n]+ -> channel(HIDDEN);