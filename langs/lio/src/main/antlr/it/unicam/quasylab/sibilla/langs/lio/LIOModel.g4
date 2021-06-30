grammar LIOModel;

@header {
package it.unicam.quasylab.sibilla.langs.lio;
}

model       : element* EOF;

element     : action
            | constant
            | state
            | system
            | param
            ;

param       : 'param' name=ID '=' value=expr ';'
            ;

constant    : ('constant'|'const') name=ID '='  value=expr ';'
            ;

action      : 'action' name=ID '=' probability=expr ';'
            ;

state       : 'state' name=ID '{'
                    (steps+=step ('+' steps+=step)*)?
               '}'
            ;

step        : performedAction=ID '.' nextState=ID
            ;

system      : 'system' name=ID ('('args += ID (',' args += ID)* ')')? '=' population += agent_expr ('|' population+=agent_expr)* ';'
            ;

agent_expr  : name=ID ('[' size=expr ']')?
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
    | '%' agent=ID                                       # populationFractionExpression
    | '#' agent=ID                                       # populationSizeExpression
//    | 'now'                                        # nowExpression
    | reference=ID                                 # referenceExpression
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


