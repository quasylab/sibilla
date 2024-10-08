grammar YodaModel;

@header {
package it.unicam.quasylab.sibilla.langs.yoda;
}

model: element* EOF;

element : constantDeclaration
        | parameterDeclaration
        | typeDeclaration
        | agentDeclaration
        | sceneElementDeclaration
        | systemDeclaration
        | groupDeclaration
        | measureDeclaration
        | predicateDeclaration
        | configurationDeclaration
        | functionDeclaration;

functionDeclaration: 'let' name=ID '(' (types += type args += ID (',' types += type args += ID)*)? ')' '=' functionStatement 'end';

functionStatement:
    'let' names += ID '=' values += expr ('and' names += ID '=' values += expr)* 'in' functionStatement 'endlet'                                  # functionStatementLet
    | 'if' guard=expr 'then' thenStatement=functionStatement 'else' elseStatement=functionStatement 'endif' # functionStatementIfThenElse
    | 'return' expr                                                             # functionStatementReturn
;

measureDeclaration: 'measure' name=ID '=' measure=expr ';';

predicateDeclaration: 'predicate' name=ID '=' predicate=expr ';';

groupDeclaration: 'group' name=ID '{' (agents += ID (',' agents += ID)* )? '}';

//PARAMETERS

constantDeclaration: 'const' name = ID '=' value=expr ';';

parameterDeclaration: 'param' name=ID '=' value=expr ';';

typeDeclaration:
    'type' typeName=ID '{'
    (fields += recordFieldDeclaration (';' fields += recordFieldDeclaration)*)?
'}';

recordFieldDeclaration: type name=ID;


//AGENT GRAMMAR

agentDeclaration:
    'agent' agentName=ID  '='
        ('state'|'conditions') ':'
            (agentStateAttributes += nameDeclaration ';')*
        'features' ':'
            (agentFeaturesAttributes += nameDeclaration ';')*
        'observations' ':'
            (agentObservationsAttributes += nameDeclaration ';')*
        'actions' ':' actionBody*
        'behaviour' ':' behaviourDeclaration
    'end';

sceneElementDeclaration:
    'element' agentName=ID '='
            (elementFeaturesAttributes += nameDeclaration ';')*
    'end';


nameDeclaration: type name=ID '=' value=expr;

actionBody: actionName=ID '[' (body += attributeUpdate)*  ']' /*
          | 'wait'    //the agent waits a turn
          | 'stop'    //the agent stops
          | 'rtf'     //the agent return to the Force */
;


attributeUpdate:
    'let' names += ID '=' values += expr ('and' names += ID '=' values += expr)* 'in' (body += attributeUpdate)+'endlet'
                                                                  # attributeUpdateLetBlock
    | fieldName=ID '<-' value=expr ';'                                # attributeUpdateAssignment
    | 'if' guard = expr 'then' (thenBlock += attributeUpdate)* ('else' (elseBlock += attributeUpdate)+)? 'endif'
                                                                  # attributeUpdateIfThenElse
    ;




behaviourDeclaration: ('when' cases+=ruleCase ('orwhen' cases+=ruleCase)* 'otherwise')? ('[' defaultcase+=weightedAction* ']')?;

ruleCase: guard=expr '->' '[' (actions+=weightedAction)* ']';

weightedAction: actionName=ID ':' weight=expr ';' ;

//SYSTEM GRAMMAR

systemDeclaration:
    'environment' ':'
        ('global'  ':' (globalAttributes += attributeUpdate)* )?
        ('sensing' ':' (agentSensing += agentAttributesUpdate)* )?
        ('dynamic' ':' (agentDynamics += agentAttributesUpdate)* )?
    'end'
;

agentAttributesUpdate: agentName=ID '[' (updates += attributeUpdate)* ']';


//CONFIG GRAMMAR

configurationDeclaration: 'configuration' name=ID ':'
    (collectives += collectiveExpression)*
'end';

collectiveExpression:
        elementName=ID '[' (init += fieldAssignment ';')* ']'                # collectiveExpressionIndividual
        | 'for' name=ID setOfValues 'do' (body += collectiveExpression)* 'endfor'            # collectiveExpressionFor
        | 'if' guard=expr 'then' (thenCollective+=collectiveExpression)* ('else' (elseeCollective+=collectiveExpression)*)? 'endif' # collectiveExpressionIfElse
;

setOfValues:
    'from' from=expr 'to' to=expr                                           # setOfValuesInterval
    | 'in' '{' values += expr (',' values += expr) '}'                      # setOfValuesEnumeration
    | 'sampled' (distinct='distinct')? size = expr 'time' 'from' generator=expr      # setOfValuesRandom
;

//UTIL

expr    : INTEGER                                                            # expressionInteger
        | REAL                                                               # expressionReal
        | 'false'                                                            # expressionFalse
        | 'true'                                                             # expressionTrue
        | reference=ID ('(' (params += expr (',' params+=expr )*)? ')')?     # expressionReference
        | '(' expr ')'                                                       # expressionBrackets
        | oper=('+'|'-') arg=expr                                            # expressionUnary
        | leftOp=expr oper=('+'|'-') rightOp=expr                            # expressionAddSubOperation
        | leftOp=expr oper=('*'|'/') rightOp=expr                            # expressionMultDivOperation
        | leftOp=expr oper=('%'|'//') rightOp=expr                           # expressionAdditionalOperation
        | leftOp=expr '^' rightOp=expr                                       # expressionPowOperation
        | '!' argument=expr                                                  # expressionNegation
        | 'sqrt' '(' argument=expr ')'                                       # expressionSquareRoot
        | leftOp=expr oper=('&'|'&&') rightOp=expr                           # expressionAnd
        | leftOp=expr oper=('|'|'||') rightOp=expr                           # expressionOr
        | leftOp=expr '->' rightOp=expr                                      # expressionImplication
        | leftOp=expr oper=('<'|'<='|'=='|'!='|'>='|'>') rightOp=expr        # expressionRelation
        | guardExpr=expr '?' thenBranch=expr ':' elseBranch=expr             # expressionIfThenElse
        | 'if' '(' guardExpr=expr ')' thenBranch=expr 'else' elseBranch=expr # expressionIfThenElse
        | '[' fieldAssignment (',' fieldAssignment)* ']'                     # expressionRecord
        | 'U''['min=expr',' max=expr']'                                      # expressionWeightedRandom
        | 'rnd'                                                              # expressionRandom
        | 'all' (groupName=ID)? ':' expr                                     # expressionForAll
        | 'any' (groupName=ID)? ':' expr                                     # expressionExists
        | 'min' (groupName=ID)? ('[' guard=expr ']' )? '.' value=expr        # expressionMinimum
        | 'max' (groupName=ID)? ('[' guard=expr ']' )? '.' value=expr        # expressionMaximum
        | 'sum' (groupName=ID)? ('[' guard=expr ']' )? '.' value=expr        # expressionSum
        | 'mean' (groupName=ID)? ('[' guard=expr ']' )? '.' value=expr       # expressionMean
        | '#' (groupName=ID)? ('[' guard=expr ']' )?                         # expressionCount
        | '%' (groupName=ID)? ('[' guard=expr ']' )?                         # expressionFraction
        |  record=expr '.' fieldName =ID                                     # expressionRecordAccess
        | 'it.' ref=ID                                                       # expressionItselfRef
        | 'sin' '(' argument=expr ')'                                        # expressionSin
        | 'sinh' '(' argument=expr ')'                                       # expressionSinh
        | 'asin' '(' argument=expr ')'                                       # expressionAsin
        | 'cos' '(' argument=expr ')'                                        # expressionCos
        | 'cosh' '(' argument=expr ')'                                       # expressionCosh
        | 'acos' '(' argument=expr ')'                                       # expressionAcos
        | 'tan' '(' argument=expr ')'                                        # expressionTan
        | 'tanh' '(' argument=expr ')'                                       # expressionTanh
        | 'atan' '(' argument=expr ')'                                       # expressionAtan
        | 'atan2' '(' left=expr ',' right=expr ')'                           # expressionAtan2
        | 'ceil' '(' argument=expr ')'                                       # expressionCeiling
        | 'floor' '(' argument=expr ')'                                      # expressionFloor
        | 'abs' '(' argument=expr ')'                                        # expressionAbsolute
        | 'PI'                                                               # expressionPi
        | 'distance' '(' a1=expr ',' a2=expr ',' a3=expr ',' a4=expr ')'     # expressionDistance
        | 'angleOf' '(' a1=expr ',' a2=expr ',' a3=expr ',' a4=expr ')'      # expressionAngleOf
        | 'dt'                                                               # expressionDt
;


fieldAssignment: name=ID '=' value=expr;

type    : 'int'                                                     # typeInteger
        | 'real'                                                    # typeReal
        | 'bool'                                                    # typeBoolean
/*        | 'char'                                                    # typeCharacter
        | 'string'                                                  # typeString
        | 'array[' type (',' type)* ']'                             # typeArrayMultipleTypes */
        | name=ID                                                   # typeCustom
;


fragment DIGIT  :   [0-9];
fragment LETTER :   [a-zA-Z_];

ID              :   LETTER (DIGIT|LETTER)*;
INTEGER         :   DIGIT+;
REAL            :   ((DIGIT* '.' DIGIT+)|DIGIT+ '.')(('E'|'e')('-')?DIGIT+)?;

COMMENT         : '/*' .*? '*/' -> channel(HIDDEN); // match anything between /* and */
WS              : [ \r\t\u000C\n]+ -> channel(HIDDEN);