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
    'type' typeName=ID '{'
        typeBody
    '}';

typeBody: (fields += fieldDeclaration (';' fields += fieldDeclaration)*)? ;

//AGENT GRAMMAR

agentDeclaration:
    'agent' agentName=ID '{'
        'state' '{' knowledgeDeclaration informationDeclaration '}'
        'observations' '{' observationDeclaration '}'
        'actions' '{' actionBody ('|' actionBody)*'}'
        'behaviour' '{'behaviourDeclaration'}'
    '}';

knowledgeDeclaration: 'knowledge' '{' (fields += fieldDeclaration (';' fields += fieldDeclaration)*)? '}';

informationDeclaration: 'information' '{' (fields += fieldDeclaration (';' fields += fieldDeclaration)*)? '}';

observationDeclaration: 'observations' '{' (fields += fieldDeclaration (';' fields += fieldDeclaration)*)?  '}';

newField: type fieldName=ID ';' ;

actionBody: actionName=ID '{' (fieldUpdate)* '}'
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

assignmentTemp: 'let' tempName=ID '{' (blocks += selectionBlock (';' blocks += selectionBlock)*)? '}';

selectionBlock: 'select' name=ID 'in' groupName=ID 'that' expr;

agentSensing: agentName=ID '{' (fieldUpdate)* '}';

evolutionDeclaration: entityName=ID '{' (fieldUpdate)* '}';

//CONFIG GRAMMAR

configurationDeclaration: 'configuration' name=ID '{'
    (collectionDeclaration)?
    systemName=ID ('[' (sceneField += fieldInit (',' sceneField += fieldInit)*)?  ']')? '{'(collectiveDeclaration)*'}'
    '}';

collectionDeclaration:
    'let' collections+=collectionAssignment
    ('and' collections+=collectionAssignment)*
    'in'
    ;

collectionAssignment:  collectionName=ID '=' func;

collectiveDeclaration: collectionName=ID '{' (fieldInit)* '}'                               #collectiveTerminal
                     | 'for' name=ID 'in' groupName=ID '{' collectiveDeclaration '}'        #collectiveFor
                     | ('if' exprBool=expr '{'collectiveDeclaration'}')+
                       ('else''{'collectiveDeclaration'}')?                                 #collectiveIfElse
;

fieldInit: fieldName=ID '=' value=expr;

//UTIL

expr    : INTEGER                                                     # expressionInteger
        | REAL                                                        # expressionReal
        | 'false'                                                     # expressionFalse
        | 'true'                                                      # expressionTrue
        | reference=ID                                                # expressionReference
        | '(' expr ')'                                                # expressionBrackets
      //  | gexpr                                                     # gexprCall
        | oper=('+'|'-') arg=expr                                     # expressionUnary
        | leftOp=expr oper=('+'|'-') rightOp=expr                     # expressionAddSubOperation
        | leftOp=expr oper=('*'|'/') rightOp=expr                     # expressionMultDivOperation
        | leftOp=expr oper=('%'|'//') rightOp=expr                    # expressionAdditionalOperation
        | leftOp=expr '^' rightOp=expr                                # expressionExponentOperation
        | '!' argument=expr                                           # expressionNegation
        | 'sqrt' '(' argument=expr ')'                                # expressionSquareRoot
        | leftOp=expr oper=('&'|'&&') rightOp=expr                    # expressionAnd
        | leftOp=expr oper=('|'|'||') rightOp=expr                    # expressionOr
        | leftOp=expr oper=('<'|'<='|'=='|'!='|'>='|'>') rightOp=expr # expressionRelation
        | guardExpr=expr '?' thenBranch=expr ':' elseBranch=expr      # expressionIfThenElse
        | '[' fieldAssignment (',' fieldAssignment)* ']'              # expressionRecord
        | 'U''['min=expr',' max=expr']'                               # expressionWeightedRandom
        | 'rnd'                                                       # expressionRandom
        | parent=ID '.' son=ID                                        # expressionAttributeRef
        | 'forall' expr ('for' name=ID 'in' groupName=ID)?            # expressionForAll
        | 'exists' expr ('for' name=ID 'in' groupName=ID)?            # expressionExists
        | 'min'    expr ('for' name=ID 'in' groupName=ID)?            # expressionMinimum
        | 'max'    expr ('for' name=ID 'in' groupName=ID)?            # expressionMaximum
        | 'it.' ref=ID                                                # expressionItselfRef
        | 'sin' '(' argument=expr ')'                                 # expressionSin
        | 'sinh' '(' argument=expr ')'                                # expressionSinh
        | 'asin' '(' argument=expr ')'                                # expressionAsin
        | 'cos' '(' argument=expr ')'                                 # expressionCos
        | 'cosh' '(' argument=expr ')'                                # expressionCosh
        | 'acos' '(' argument=expr ')'                                # expressionAcos
        | 'tan' '(' argument=expr ')'                                 # expressionTan
        | 'tanh' '(' argument=expr ')'                                # expressionTanh
        | 'atan' '(' argument=expr ')'                                # expressionAtan
        | 'ceil' '(' argument=expr ')'                                # expressionCeiling
        | 'floor' '(' argument=expr ')'                               # expressionFloor
;

type    : 'int'                                                     # typeInteger
        | 'real'                                                    # typeReal
        | 'bool'                                                    # typeBoolean
        | 'char'                                                    # typeCharacter
        | 'String'                                                  # typeString
        | 'array[' type (',' type)* ']'                             # typeArrayMultipleTypes
        | typeDeclaration                                           # typeNew
;

func    : 'generate' '(' size=expr ',' randomExpression=expr ')'                 # functionGenerate
        | 'distinct' '(' size=expr ',' randomExpression=expr ')'                 # functionDistinct
        | 'distinctFrom''(' size=expr ',' randomExpression=expr ',' other=ID ')' # functionDistinctFrom
        ;

fragment DIGIT  :   [0-9];
fragment LETTER :   [a-zA-Z_];

ID              :   LETTER (DIGIT|LETTER)*;
INTEGER         :   DIGIT+;
REAL            :   ((DIGIT* '.' DIGIT+)|DIGIT+ '.')(('E'|'e')('-')?DIGIT+)?;

COMMENT         : '/*' .*? '*/' -> channel(HIDDEN); // match anything between /* and */
WS              : [ \r\t\u000C\n]+ -> channel(HIDDEN);