grammar SibillaScript;

@header {
package it.unicam.quasylab.sibilla.shell;
}

script  : (command)* EOF;

command : module_command
        | seed_command
        | load_command
        | seed_command
        | load_command
        | environment_command
        | set_command
        | clear_command
        | reset_command
        | modules_command
        | state_command
        | states_command
        | info_command
        | replica_command
        | deadline_command
        | dt_command
        | measures_command
        | add_measure_command
        | remove_measure_command
        | load_properties_command
        | formulas_command
        | check_command
        | save_command
        | simulate_command
        | quit_command
        | run_command
        | cwd_command
        | cd_command
        | ls_command
        | add_all_measures_command
        | remove_all_measures_command
        | descriptive_statistics
        | summary_statistics
        | show_statistics
        | predicates_command
        | first_passage_time
        | reachability_command
        | set_optimization_strategy
        | set_optimization_properties
        | set_surrogate_properties
        | search_space_interval
        | constraints_definition
        | optimization_command
        | dataset_setting
        | reset_optimization_command
        | sample_command
        | save_samples_command
        ;

reachability_command: 'probreach' goal=STRING ('while' condition=STRING)? 'with' 'alpha' '='  alpha=REAL 'and' 'delta' '=' delta=REAL;

first_passage_time: 'fpt' name=STRING;

show_statistics: 'show' 'statistics';

summary_statistics: 'summary' 'statistics';

descriptive_statistics: 'descriptive' 'statistics';

quit_command: 'quit';

module_command : 'module' name=STRING
        ;

seed_command : 'seed' (value=REAL)?
        ;

load_command : 'load' value=STRING
        ;

environment_command : ('env'|'environment')
        ;

set_command : 'set' name=STRING value=REAL
        ;

clear_command : 'clear'
        ;

reset_command : 'reset' (name=STRING)?
        ;

modules_command : 'modules'
        ;

state_command : 'init' name=STRING ('(' values += REAL (',' values += REAL)* ')')?
        ;

states_command: 'states'
        ;

info_command : 'info'
        ;

replica_command : 'replica' (value=INTEGER)?
        ;

deadline_command : 'deadline' (value=(REAL|INTEGER))?
        ;

dt_command : 'dt' (value=(REAL|INTEGER))?
        ;

measures_command : 'measures'
        ;

predicates_command : 'predicates'
        ;

add_measure_command : 'add' 'measure' name=STRING
        ;

add_all_measures_command : 'add' 'all' 'measures' ;

remove_measure_command : 'remove' 'measure' name=STRING
        ;

remove_all_measures_command : 'remove' 'all' 'measures'
        ;

load_properties_command : ('properties'|'prop') file=STRING
        ;

formulas_command : 'formulas'
        ;

check_command : 'check' name=STRING ('(' args += REAL (',' args+= REAL )* ')') ('[' cargs+=command_argument (',' cargs+=command_argument)* ']')?
        ;

command_argument: name=ID '=' value=REAL;

save_command : 'save' (name=ID)? ('output'  dir=STRING)? ('prefix' prefix=STRING)? ('postfix' postfix=STRING)?
            ;

simulate_command : 'simulate' (label=ID)?;

run_command: 'run' name=STRING ;

cwd_command : 'cwd' ;

ls_command : 'ls' ;

cd_command : 'cd' name=STRING;


set_optimization_strategy :
    'optimizes using' algorithm_name=STRING ('with surrogate' surrogate_name=STRING)?
;

set_optimization_properties :
    ('set' 'optimization' 'property'|'set' 'opt' 'prop') key=STRING value=STRING
;

set_surrogate_properties :
    ('set' 'surrogate' 'property'|'set' 'sur' 'prop') key=STRING  value=STRING
;

search_space_interval :
    'search' 'in' variable=STRING 'in' '[' lower_bound=expr',' upper_bound=expr ']'
;

constraints_definition :
    'add' 'constraint' constraint=expr
;

dataset_setting :
    'dataset' 'size' dataset_size=INTEGER ('test' test_percentage=REAL)? ('sampling' sampling_strategy_name=STRING)?
;

optimization_command :
    kind_of_optimization=( 'minimize' | 'min' | 'maximize' | 'max' ) objective_function
;

sample_command :
    'sample' ('using' sampling_strategy=STRING)? objective_function ('of size' number_of_samples=INTEGER)?
 ;

save_samples_command : 'save' 'samples' (name=ID)? ('output'  dir=STRING)? ('prefix' prefix=STRING)? ('postfix' postfix=STRING)?
            ;

objective_function : (
     objective_reachability
     | objective_first_passage_time
     | objective_expr
     );

reset_optimization_command : 'reset' 'optimization' 'setting';

objective_reachability : 'probreach' goal=STRING (('while' condition=STRING)? 'with' 'alpha' '='  alpha=REAL 'and' 'delta' '=' delta=REAL)?;
objective_first_passage_time : 'fpt' name=STRING;
objective_expr : expression=expr ;

expr :
      left=expr op=('&'|'&&') right=expr                      # andExpression
    | left=expr op=('|'|'||') right=expr                      # orExpression
    | <assoc=right> left=expr '^' right=expr                  # exponentExpression
    | left=expr op=('*'|'/'|'//') right=expr                  # mulDivExpression
    | left=expr op=('+'|'-'|'%') right=expr                   # addSubExpression
    | left=expr op=('<'|'<='|'=='|'>='|'>') right=expr        # relationExpression
    | '!' arg=expr                                            # negationExpression
    | guard=expr '?' thenBranch=expr ':' elseBranch=expr      # ifThenElseExpression
    | op=('-'|'+') arg=expr                                   # unaryExpression
    | '(' expr ')'                                            # bracketExpression
    | INTEGER                                                 # intValue
    | REAL                                                    # realValue
    | ('-inf'|'+inf')                                         # infinity
    | 'false'                                                 # falseValue
    | 'true'                                                  # trueValue
    | reference=ID                                            # referenceExpression
    | 'abs' '(' expr ')'                                      # absoluteValue
    ;

fragment DIGIT  :   [0-9];
fragment LETTER :   [a-zA-Z_];

ID              :   LETTER (DIGIT|LETTER)*;
INTEGER         :   DIGIT+;
REAL            :   ((DIGIT* '.' DIGIT+)|DIGIT+ '.')(('E'|'e')('-')?DIGIT+)?;

STRING          : '"' ( ~["\n\r] | '\\"')* '"' ;

COMMENT
    : '/*' .*? '*/' -> channel(HIDDEN) // match anything between /* and */
    ;

WS  : [ \r\t\u000C\n]+ -> channel(HIDDEN)
    ;
