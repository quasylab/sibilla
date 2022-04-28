grammar YodaModel;

@header {
package it.unicam.quasylab.sibilla.langs.yoda;
}

model: element* EOF;

element : constant_declaration
        | parameter_declaration
        | type_declaration
        | agent_declaration
        | system_declaration
        | configuration_declaration;

//PARAMETERS

constant_declaration: 'const' name=ID '=' value=expr ';';

parameter_declaration: 'param' name=ID '=' value=expr ';';

type_declaration:'type' name=ID '{'
                        type var_name=ID (';'type var_name=ID)*
                    '}'
                ;

//AGENT GRAMMAR

agent_declaration:
    'agent' name=ID '(' constr_params (','constr_params)* ')' '{'
    'state' '{'state_declaration (';' state_declaration)* '}'
    'observations' '{'observation_declaration (';' observation_declaration)* '}'
    'actions' '{' action_declaration ('|' action_declaration)*'}'
    'behaviour' '{'behaviour_declaration'}'
    '}'
    ;

constr_params: type? name=ID;

state_declaration: type name=ID ('<-' value=expr)? ;

observation_declaration: type name=ID ;

action_declaration: action_name=ID '{'
    action_body(';'action_body)*
    '}'
    ;

action_body: terminal_action_body
           | agent_ref_action_body
           ;

terminal_action_body: state_name=ID '<-' value=expr;

agent_ref_action_body: agent_reference=ID'{'(terminal_action_body)*'}';

behaviour_declaration: name=ID '{'
    (behaviour_rule)*
    def_behaviour_rule
    '}'
    ;

behaviour_rule:
    '['guardExpr=expr('|' guardExpr=expr)*']'
    '->'
    action_name=ID ':' weight=expr ';';

def_behaviour_rule: 'default' action_name=ID ':' weight=expr ';';

//WORLD GRAMMAR

system_declaration:
    'system' name=ID '(' constr_params (','constr_params)* ')' '{'
    'global' '{' global_state_declaration '}'
    'sensing' '{' sensing_declaration '}'
    'actions' '{' action_declaration '}'
    'evolution' '{'env_evolution_declaration'}'
    '};'
    ;

global_state_declaration: '{'
    (global_field_declaration)+
    '};'
    ;

global_field_declaration:scene_field
                        |hidden_field
                        ;

scene_field: type field_name=ID ('<-' value=expr)? ';';

hidden_field: agent_name=ID '{'
               type name_var=ID ';' (type name_var=ID';')*
             '};' ;

sensing_declaration:'{'
    (agent_sensing)*
    '}';

agent_sensing: agent_name=ID'{'
    (sensing_name=ID '<-' value=expr )*
    '}' ;

env_evolution_declaration: 'environment' name=ID '=' '{'
    //(environment_rule)?
    def_env_evolution_rule
    '};'
    ;

//environment_rule:;

def_env_evolution_rule:'default' env_rule=ID ':' weight=expr ';';

//SYSTEM CONFIG

configuration_declaration: 'configuration' name=ID '{'
    (assignment_declaration)?
    collective_declaration
    '}';

assignment_declaration:
    'let' name=ID '=' func
    ('and' name=ID '=' func)*
    'in'
    ;

collective_declaration:
    collective_name=ID ('{'collective_body'}')*;

collective_body: collection_name=ID '<-' expr ';'
               | collection_name=ID '{'collective_body*'}'
               | 'for' name=ID 'in' group_name=ID '{'collective_body'}'
               | ('if' expr_bool=expr '{'collective_body'}')+
                 //('if' expr_bool=expr '{'collective_body'}')*
                 ('else''{'collective_body'}')?
               ;

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
        | leftOp=expr oper=('&'|'&&') rightOp=expr                  # andExpression
        | leftOp=expr oper=('|'|'||') rightOp=expr                  # orExpression
        | leftOp=expr oper=('<'|'<='|'=='|'>='|'>') rightOp=expr    # relationExpression
        | guardExpr=expr '?' thenBranch=expr ':' elseBranch=expr    # ifthenelseExpression
        | '[' fieldAssignment (',' fieldAssignment)* ']'            # recordExpression
        | 'U''['min=expr',' max=expr']'                             # weightedRandomExpression
        | 'rnd'                                                     # randomExpression
        | parent=ID '.' son=ID                                      # attributeRef
        | 'forall' name=ID 'in' group_name=ID ':' expr              # forallExpression
        | 'exists' name=ID 'in' group_name=ID ':' expr              # existsExpression
        | 'min'    name=ID 'in' group_name=ID ':' expr              # minimumExpression
        | 'max'    name=ID 'in' group_name=ID ':' expr              # maximumExpression
        | 'it.' ID                                                  # itselfRef
        ;

fieldAssignment : name=ID '=' expr;
/*
gexpr   : expr                                                      # expression
        | parent=ID '.' son=ID                                      # attributeRef
        | 'forall' name=ID 'in' group_name=ID ':' gexpr             # forallExpr
        | 'exists' name=ID 'in' group_name=ID ':' gexpr             # existsExpr
        | 'min'    name=ID 'in' group_name=ID ':' gexpr             # minimumExpr
        | 'max'    name=ID 'in' group_name=ID ':' gexpr             # maximumExpr
        | 'it.' ID                                                  # itself
        ;
*/

type    : 'int'                                                     # integerNumber
        | 'real'                                                    # realNumber
        | 'bool'                                                    # boolean
        | 'char'                                                    # character
        | 'String'                                                  # string
        | 'array[' type (',' type)* ']'                             # arrayMultipleTypes
        | type_declaration                                          # newType
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