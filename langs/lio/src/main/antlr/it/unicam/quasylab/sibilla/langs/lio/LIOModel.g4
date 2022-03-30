grammar LIOModel;

@header {
package it.unicam.quasylab.sibilla.langs.lio;
}

model       : (modelContent+=element)* EOF;

element     : elementAction
            | elementConstant
            | elementState
            | elementSystem
            | elementParam
            | elementMeasure
            | elementAtomic
            | elementPredicate
            ;

elementParam:
            'param' name=ID '=' value=expr ';'
            ;

elementConstant:
            ('constant'|'const') name=ID '='  value=expr ';'
            ;

elementAction:
            'action' name=ID '=' probability=expr ';'
            ;

elementState:
            'state' name=ID '{'
                (steps+=agentStep ('+' steps+=agentStep)*)?
            '}'
            ;

elementMeasure:
            'measure' name=ID '=' value=expr ';'
            ;

elementAtomic:
            'atomic' name=ID '=' '{' states += ID (',' states += ID)* '}' ';'
            ;

elementPredicate:
            'predicate' name=ID '=' value=expr ';'
            ;

agentStep:
            performedAction=ID '.' nextState=ID
            ;

elementSystem:
            'system' name=ID ('('args += ID (',' args += ID)* ')')? '=' population += agentExpression ('|' population+=agentExpression)* ';'
            ;

agentExpression:
            name=ID ('[' size=expr ']')?
            ;

expr:
            left=expr op=('<'|'<='|'=='|'>='|'>') right=expr          # expressionRelation
            | left=expr op=('&'|'&&') right=expr                        # expressionConjunction
            | left=expr op=('|'|'||') right=expr                        # expressionDisjunction
            | left=expr '^' right=expr                                  # expressionPower
            | left=expr op=('*'|'/'|'//') right=expr                    # expressionMulDiv
            | left=expr op=('+'|'-'|'%') right=expr                     # expressionSumDiff
            | '!' arg=expr                                              # expressionNegation
            | guard=expr '?' thenBranch=expr ':' elseBranch=expr        # expressionIfThenElse
            | op=('-'|'+') arg=expr                                     # expressionUnary
            | '(' expr ')'                                              # expressionBracket
            | INTEGER                                                   # expressionInteger
            | REAL                                                      # expressionReal
            | 'false'                                                   # expressionFalse
            | 'true'                                                    # expressionTrue
            | '%' agent=ID                                              # expressionFractionOfAgents
            | '#' agent=ID                                              # expressionNumberOfAgents
            | reference=ID                                              # expressionReference
            ;


fragment DIGIT  :   [0-9];
fragment LETTER :   [a-zA-Z_];

ID              :   LETTER (DIGIT|LETTER)*;
INTEGER         :   DIGIT+;
REAL            :   ((DIGIT* '.' DIGIT+)|DIGIT+ '.')(('E'|'e')('-')?DIGIT+)?;

INFO
    : '/**' .*? '*/' -> channel(HIDDEN) // match anything between /* and */
    ;

COMMENT
    : '/*' .*? '*/' -> channel(HIDDEN) // match anything between /* and */
    ;

WS  : [ \r\t\u000C\n]+ -> channel(HIDDEN)
    ;

/*

Element:
	  Action
	| Constant
	| StateConstant
	| Configuration
	| Label
	| Formula
	| PathFormula
;

PathFormula :
	'path' ('formula')? name=ID ':' formula=PctlPathFormula ';'
;


Formula:
	'formula' name=ID ':' formula=Expression ';'
;


Label:
	'label' name=ID ':' '{' (states += [StateConstant] (',' states+=[StateConstant] ) )? '}'
;


Constant:
	('constant'|'const') name=ID '=' exp=Expression ';'
;

Action:
	'action' name=ID ':' probability=Expression ';'
;

StateConstant:
	'state' name=ID '{'
		(transitions += Transition ('+' transitions += Transition)*)?
	'}'
;

Transition:
	action = [Action] '.' nextState=[StateConstant]
;

Configuration:
	'system' name=ID '=' '<' elements += PopulationElement (',' elements += PopulationElement )* '>' ';'
;

PopulationElement:
	state=[StateConstant] (hasSize ?= '[' size=Expression ']')?
;


RelationExpression returns Expression:
	SumDiffExpression ({RelationExpression.left = current} op=RelationSymbol right=SumDiffExpression )?
;

SumDiffExpression returns Expression:
	MulDivExpression  ({SumDiffExpression.left = current} op=('+'|'-') right=SumDiffExpression )?
;

MulDivExpression returns Expression:
	BaseExpression  ({MulDivExpression.left = current} op=('*'|'/') right=MulDivExpression )?
;

BaseExpression returns Expression:
	NumberExpression
	| NotFormula
	| TrueFormula
	| FalseFormula
	| ProbabilityFormula
	| LiteralExpression
	| PopulationExpression
	| '(' Expression ')'
	| LogExpression
	| ModExpression
	| PowExpression
	| FloorExpression
	| CeilExpression
	| MinExpression
	| MaxExpression
	| SinExpression
	| CosExpression
	| TanExpression
	| ATanExpression
	| ASinExpression
	| ACosExpression
;

LiteralExpression:
	ref=[ReferenceableName]
;


ModExpression:
	'mod' '(' arg=Expression ',' mod=Expression ')'
;

LogExpression:
	'ln' '(' arg=Expression ')'
;

PowExpression:
	'pow' '(' base=Expression ',' exp=Expression ')'
;

FloorExpression:
	'floor' '(' arg=Expression ')'
;

CeilExpression:
	'ceil' '(' arg=Expression ')'
;

MinExpression:
	'min' '(' left = Expression ',' right = Expression  ')'
;

MaxExpression:
	'man' '(' left = Expression ',' right = Expression ')'
;

SinExpression:
	'sin' '(' arg = Expression ')'
;

CosExpression:
	'cos' '(' arg = Expression ')'
;

TanExpression:
	'tan' '(' arg = Expression ')'
;

ATanExpression:
	'atan' '(' arg = Expression ')'
;

ASinExpression:
	'asin' '(' arg = Expression ')'
;

ACosExpression:
	'acos' '(' arg = Expression ')'
;


PopulationExpression: {PopulationExpression}
	'frc' '('state = [StateConstant] ')'
;

NumberExpression:
	DecimalLiteral
;


DecimalLiteral returns NumberExpression:
	NumberLiteral ({DecimalLiteral.integerPart = current} decimalPart=DECIMAL)?
;

terminal DECIMAL: '.'('0'..'9')*;


NumberLiteral:
	intPart=INT
;

ReferenceableName:
	Label|Formula|Constant|StateConstant
;

Expression:
	PctlExpression
;

PctlExpression returns Expression:
	OrPctlFormula
;

OrPctlFormula returns Expression:
	AndPctlFormula ({OrPctlFormula.left=current} '|' right=OrPctlFormula )?
;

AndPctlFormula returns Expression:
	RelationExpression ({AndPctlFormula.left=current} '&' right=AndPctlFormula)?
;

ProbabilityFormula:
	'P' '{' rel=RelationSymbol pBound=NumberExpression '}' '[' path=PctlPathFormula ']'
;


enum RelationSymbol:
	LES='<' |
	LEQ='<='|
	GEQ='>='|
	GTR='>'
;

PctlPathFormula:
	NextFormula
	| UntilFormula
	| NamedPctlPathFormula
;

NamedPctlPathFormula:
	name=[PathFormula]
;


NextFormula:
	'X' arg=Expression
;

UntilFormula:
	left=RelationExpression 'U' (isBound ?= '<=' bound=INT)? right=RelationExpression
;

FalseFormula: {FalseFormula}
	'false'
;


TrueFormula: {TrueFormula}
	'true'
;


NotFormula:
	'!' arg=BaseExpression
;
*/


