package it.unicam.quasylab.sibilla.core.runtime.command;

public enum CommandName {
    LOAD_MODULE,
    LOAD_MODULE_SPECIFICATION_FROM_FILE,
    SET_PARAMETER,
    CLEAR,
    RESET,
    RESET_PARAMETER,
    SET_MEASURES,
    GET_MODULES,
    GET_PARAMETERS,
    GET_PARAMETER,
    GET_EVALUATION_ENVIRONMENT,
    LOAD,
    INFO,
    GET_INITIAL_CONFIGURATIONS,
    GET_CONFIGURATION_INFO,
    SET_CONFIGURATION,
    GET_MEASURES,
    ADD_MEASURE,
    REMOVE_MEASURE,
    ADD_ALL_MEASURES,
    REMOVE_ALL_MEASURES,
    SIMULATE,
    SET_DEADLINE,
    GET_DEADLINE,
    SET_DT,
    GET_DT,
    SET_REPLICA,
    GET_REPLICA,
    SAVE,
    SAVE_TABLE,
    PRINT_DATA,
    GET_PREDICATES,
    FIRST_PASSAGE_TIME,
    COMPUTE_PROB_REACH,
    SET_MODE,
    GET_MODES,
    GET_MODE,
    SET_SEED,
    GET_SEED,
    USE_DESCRIPTIVE_STATISTICS,
    USE_SUMMARY_STATISTICS,
    IS_DESCRIPTIVE_STATISTICS,
    IS_SUMMARY_STATISTICS,
    GET_STATISTICS,
    SET_SIMULATION_MANAGER_FACTORY,
    TRACE,
    SET_OPTIMIZATION_STRATEGY,
    SET_SURROGATE_STRATEGY,
    USING_SURROGATE,
    SET_SURROGATE_PROPERTY,
    SET_OPTIMIZATION_PROPERTY,
    ADD_SPACE_INTERVAL,
    ADD_CONSTRAINT,
    SET_DATA_SET_SIZE,
    SET_TRAINING_SET_PROPORTION,
    SET_SAMPLING_STRATEGY,
    SET_OPTIMIZATION_AS_MINIMIZATION,
    SET_PROB_REACH_AS_OBJECTIVE_FUNCTION,
    SET_PROB_REACH_AS_OBJECTIVE_FUNCTION_WITH_CONDITION,
    SET_FIRST_PASSAGE_TIME_AS_OBJECTIVE_FUNCTION,
    SET_OBJECTIVE_FUNCTION,
    PERFORM_OPTIMIZATION,
    GET_OPTIMIZATION_INFO,
    GET_TRAINING_SET_INFO,
    GENERATE_TRAINING_SET,
    GET_TRAINING_SET,
    RESET_OPTIMIZATION_SETTINGS,
    GET_OPTIMAL_SOLUTION,
    EVALUATE_OBJECTIVE_FUNCTION,
    EVALUATE_SURROGATE_FUNCTION,
    MONITOR, LOAD_STL_FORMULAS
}
