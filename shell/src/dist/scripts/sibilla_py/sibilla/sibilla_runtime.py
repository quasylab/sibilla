from logging import exception
import os
import jnius_config

SSHELL_PATH = os.environ.get('SSHELL_PATH', '')

jnius_config.add_classpath(os.path.join(SSHELL_PATH, 'lib', '*'))


from typing import Dict, List, Callable
import jnius
import pandas as pd
import io

from .plotting_module import SibillaDataPlotter
from .profiler_module import Profiler

SimulationMonitor = jnius.autoclass("it.unicam.quasylab.sibilla.core.simulator.SimulationMonitor")
ShellSimulationMonitor = jnius.autoclass("it.unicam.quasylab.sibilla.shell.ShellSimulationMonitor")
FirstPassageTimeResults = jnius.autoclass("it.unicam.quasylab.sibilla.core.simulator.sampling.FirstPassageTime")
System = jnius.autoclass("java.lang.System")

SynthesisRecord = jnius.autoclass("it.unicam.quasylab.sibilla.tools.synthesis.SynthesisRecord")
SurrogateMetrics = jnius.autoclass("it.unicam.quasylab.sibilla.tools.synthesis.surrogate.SurrogateMetrics")



class Map(jnius.JavaClass, metaclass=jnius.MetaJavaClass):
    __javaclass__ = 'java/util/Map'
    entrySet = jnius.JavaMethod('()Ljava/util/Set;')

    def to_dict(self):
        return {e.getKey():e.getValue() for e in self.entrySet().toArray()}


class MonitorResults:
    def __init__(self, java_result, is_qualitative: bool):
        self.java_result = java_result
        self.is_qualitative = is_qualitative

    def get_formula_results(self, formula_name: str) -> List[List[float]]:
        entry_set = self.java_result.entrySet()
        iterator = entry_set.iterator()
        while iterator.hasNext():
            entry = iterator.next()
            if str(entry.getKey()) == formula_name:
                return [list(row) for row in entry.getValue()]
        return []

    def get_all_results(self) -> Dict[str, List[List[float]]]:
        result = {}
        entry_set = self.java_result.entrySet()
        iterator = entry_set.iterator()
        while iterator.hasNext():
            entry = iterator.next()
            key = str(entry.getKey())
            value = entry.getValue()
            result[key] = [list(row) for row in value]
        return result

    def get_pandas_dataframe(self, formula_name: str) -> pd.DataFrame:
        data = self.get_formula_results(formula_name)
        if not data:
            return pd.DataFrame()

        if self.is_qualitative:
            columns = ['Time Step', 'Probability']
        else:
            columns = ['Time Step', 'Mean Robustness', 'SD Robustness']

        return pd.DataFrame(data, columns=columns)

    def get_all_pandas_dataframes(self) -> Dict[str, pd.DataFrame]:
        return {formula: self.get_pandas_dataframe(formula)
                for formula in self.get_all_results().keys()}

    def __str__(self):
        return str(self.get_all_results())

    def __repr__(self):
        return f"MonitorResults({self.get_all_results()}, is_qualitative={self.is_qualitative})"

class CommandExecutionException(Exception):
    pass

class StlModelGenerationException(Exception):
    pass

class SibillaRuntime:

    def __init__(self):
        self.__runtime = jnius.autoclass("it.unicam.quasylab.sibilla.core.runtime.SibillaRuntime")()
        self.current_module = 'NOT SET'

    def init_modules(self):
        self.__runtime.initModules()

    def get_modules(self):
        return self.__runtime.getModules()

    def load_module(self, name):
        self.__runtime.loadModule(name)

    def load_from_file(self, file_name: str):
        self.__runtime.loadFromFile(file_name)

    def load(self, code: str):
        self.__runtime.load(code)

    def info(self):
        return self.__runtime.info()

    def set_parameter(self, name: str, value: float, keep_configuration : bool = False, silent : bool = True):
        self.__runtime.setParameter(name, value)
        message = name  + ' changed to '+str(value)
        if keep_configuration:
            self.set_configuration(self.current_configuration_name,*self.current_configuration_args)
            message = message + 'current configuration : '+ self.current_configuration_name
        if not silent:
            print(message)

    def get_parameter(self, name: str):
        return self.__runtime.getParameter(name)

    def get_parameters(self):
        return self.__runtime.getParameters()

    def get_evaluation_environment(self):
        try:
            return self.__runtime.getEvaluationEnvironment().to_dict()
        except jnius.JavaException:
            print("Internal Error")

    def clear(self):
        self.__runtime.clear()

    def reset(self, name: str = None):
        if str == None:
            self.__runtime.reset()
        else:
            self.__runtime.reset(name)

    def get_initial_configurations(self):
        return self.__runtime.getInitialConfigurations()

    def get_configuration_info(self, name: str):
        return self.__runtime.getConfigurationInfo(name)

    def set_configuration(self, name: str, *args: float):
        self.current_configuration_name = name
        self.current_configuration_args = args
        return self.__runtime.setConfiguration(name, *args)

    #
    # TODO : need to be removed
    #
    def check_loaded_module(self):
        self.__runtime.checkLoadedModule()

    def get_measures(self):
        return self.__runtime.getMeasures()

    def is_enabled_measure(self, name: str):
        return self.__runtime.isEnabledMeasure(name)

    def set_measures(self, *measures: str):
        return self.__runtime.setMeasures(*measures)

    def add_measures(self, *args):
        for measure in args:
            self.add_measure(measure)

    def add_measure(self, name: str):
        self.__runtime.addMeasure(name)

    def remove_measure(self, name: str):
        self.__runtime.removeMeasure(name)

    def add_all_measures(self):
        self.__runtime.addAllMeasures()

    def remove_all_measures(self):
        self.__runtime.removeAllMeasures()


    def simulate(self, label: str, monitor: SimulationMonitor = None):
        """
        perform the simulation on a defined model, to call simulate
        dt, deadline, must have been previously set
        :param str label: a label attributed to the simulation being performed
        :param SimulationMonitor monitor: a monitor for the simulation
        :return: the simulation statistics results
        :rtype: dict
        """

        def simulate_runtime(label: str, monitor: SimulationMonitor = None):
            results = None
            if monitor == None:
                results = self.__runtime.simulate(label).to_dict()
            else:
                results = self.__runtime.simulate(monitor, label).to_dict()
            #try:
            #    if monitor == None:
            #        results = self.__runtime.simulate(label).to_dict()
            #    else:
            #        results = self.__runtime.simulate(monitor, label).to_dict()
            #except:
            #    print('Internal Error')

            simulation_results = SibillaSimulationResult(results)
            return simulation_results

        profiler = Profiler(running_message="Simulating",done_message="The simulation has been successfully completed")
        s_r = profiler.execute(simulate_runtime,label,monitor)
        s_r.set_profiler(profiler)
        return s_r


    def use_descriptive_statistics(self):
        self.__runtime.useDescriptiveStatistics()

    def use_summary_statistics(self):
        self.__runtime.useSummaryStatistics()

    def is_descriptive_statistics(self):
        return self.__runtime.isDescriptiveStatistics()

    def is_summary_statistics(self):
        return self.__runtime.isSummaryStatistics()

    def get_statistics(self):
        return self.__runtime.getStatistics()

    def check_dt(self):
        self.__runtime.checkDt()

    def check_deadline(self):
        self.__runtime.checkDeadline()

    def set_deadline(self, deadline: float):
        self.__runtime.setDeadline(deadline)

    def get_deadline(self):
        return self.__runtime.getDeadline()

    def set_dt(self, dt: float):
        self.__runtime.setDt(dt)

    def get_modes(self):
        return self.__runtime.getModes()

    def set_mode(self, name: str):
        self.__runtime.setMode(name)

    def get_mode(self):
        return self.__runtime.getMode()

    def set_seed(self, seed: int):
        self.__runtime.setSeed(seed)

    def get_seed(self):
        return self.__runtime.getSeed()

    def save(self, output_folder: str, prefix: str, postfix: str, label: str = None):
        if label == None:
            return self.__runtime.save(output_folder, prefix, postfix)
        else:
            return self.__runtime.save(label,output_folder, prefix, postfix)

    def set_replica(self, replica: int):
        self.__runtime.setReplica(replica)

    def get_dt(self):
        return self.__runtime.getDt()

    def get_replica(self):
        return self.__runtime.getReplica()

    def print_data(self, label: str):
        return self.__runtime.printData(label)

    def get_predicates(self):
        return self.__runtime()

    def load_formula(self, source_code: str):
        self.__runtime.loadFormula(source_code)

    def load_formula_from_file(self, file_path: str):
        java_file = jnius.autoclass('java.io.File')(file_path)
        self.__runtime.loadFormula(java_file)


    def qualitative_monitor_signal(self, formula_names: List[str], formula_parameters: List[Dict[str, float]]) -> MonitorResults:
        # Convert formula_names to Java String array
        String = jnius.autoclass('java.lang.String')
        StringArray = jnius.autoclass('java.lang.reflect.Array')
        java_formula_names = StringArray.newInstance(String, len(formula_names))
        for i, name in enumerate(formula_names):
            java_formula_names[i] = String(name)

        # Convert formula_parameters to Java Map array
        JavaHashMap = jnius.autoclass('java.util.HashMap')
        MapArray = jnius.autoclass('java.lang.reflect.Array')
        java_formula_parameters = MapArray.newInstance(JavaHashMap, len(formula_parameters))
        for i, params in enumerate(formula_parameters):
            java_map = JavaHashMap()
            for k, v in params.items():
                java_map.put(k, float(v))
            java_formula_parameters[i] = java_map

        result = self.__runtime.qualitativeMonitorSignal(
            java_formula_names,
            java_formula_parameters
        )
        return MonitorResults(result, is_qualitative=True)

    def quantitative_monitor_signal(self, formula_names: List[str], formula_parameters: List[Dict[str, float]]) -> MonitorResults:
        # Convert formula_names to Java String array
        String = jnius.autoclass('java.lang.String')
        StringArray = jnius.autoclass('java.lang.reflect.Array')
        java_formula_names = StringArray.newInstance(String, len(formula_names))
        for i, name in enumerate(formula_names):
            java_formula_names[i] = String(name)

        # Convert formula_parameters to Java Map array
        JavaHashMap = jnius.autoclass('java.util.HashMap')
        MapArray = jnius.autoclass('java.lang.reflect.Array')
        java_formula_parameters = MapArray.newInstance(JavaHashMap, len(formula_parameters))
        for i, params in enumerate(formula_parameters):
            java_map = JavaHashMap()
            for k, v in params.items():
                java_map.put(k, float(v))
            java_formula_parameters[i] = java_map

        result = self.__runtime.quantitativeMonitorSignal(
            java_formula_names,
            java_formula_parameters
        )
        return MonitorResults(result, is_qualitative=False)

    def expected_probability_at_time0(self, formula_name, formula_parameters):
        return self.__runtime.expectedProbabilityAtTime0(formula_name, formula_parameters)

    def mean_robustness_at_time0(self, formula_name, formula_parameters):
        return self.__runtime.meanRobustnessAtTime0(formula_name, formula_parameters)

    def mean_and_sd_robustness_at_time0(self, formula_name, formula_parameters):
        result = self.__runtime.meanAndSdRobustnessAtTime0(formula_name, formula_parameters)
        return tuple(result)

    def trace(self, trace_spec: str, output_folder: str, header: bool):
        return self.__runtime.trace(trace_spec, output_folder, header)

    def evaluate_first_passage_time(self, predicate_name: str,monitor: SimulationMonitor = None):

        def ftp_runtime( predicate_name: str,monitor: SimulationMonitor = None):
            fpt_result_runtime = self.__runtime.firstPassageTime(monitor,predicate_name)
            fpt_result = SibillaFirstPassageTimeResult(fpt_result_runtime)
            return fpt_result

        profiler = Profiler(running_message="Evaluating fpt",done_message="The ftp evaluation has been successfully completed")
        fpt_r = profiler.execute(ftp_runtime, predicate_name, monitor)
        fpt_r.set_profiler(profiler)
        return fpt_r

    def perform_synthesis(self, source_spec: str):
        synthesis_record = SynthesisRecord(self.__runtime.performSynthesis(source_spec))
        return synthesis_record



    def evaluate_reachability(self,goal: str, delta:float = 0.01 , epsilon:float = 0.01, condition: str = None, monitor: SimulationMonitor=None):

        def reachability_runtime(goal : str, delta : float, epsilon : float, condition : str, monitor : SimulationMonitor = None):

            result = None

            if condition == None:
                result = self.__runtime.computeProbReach(monitor,goal,delta,epsilon)
            else:
                result = self.__runtime.computeProbReach(monitor,condition,goal,delta,epsilon)

            reachabiliy_result = SibillaReachabilityResult(result,goal,delta,epsilon,condition)
            return reachabiliy_result


        message = 'Evaluating the reachability of ' + goal

        if condition != None:
            message += ' satisfying the condition ' + condition

        profiler = Profiler(running_message = message ,done_message = "The reachability evaluation has been successfully completed")
        r_r = profiler.execute(reachability_runtime, goal, delta, epsilon, condition, monitor)
        r_r.set_profiler(profiler)
        return r_r


    @classmethod
    def from_population_model(cls, file_path : str, configuration : str = None, deadline : int = 100, dt : float = 1.0, replica : int = 100):
        #
        # TODO check this method
        #
        cls.reset()
        cls.load_module("population")
        cls.load_from_file(file_path)
        cls.intial_congiruations = cls.sr.get_initial_configurations
        if configuration == None:
            first_configuration_found = cls.intial_congiruations[0]
            cls.set_configuration(first_configuration_found)
        else:
            cls.set_configuration(configuration)
        cls.add_all_measures()
        cls.set_deadline(deadline)
        cls.set_dt(dt)
        cls.set_replica(replica)
        return cls

    def  __enter__(self):
        return self
    def __exit__(self, type, value, traceback):
        self.clear()





class SibillaSimulationResult:


    # TODO
    #
    # could be useful to have all the data of
    # the simulation dt, deadline, replica ...
    #


    def __init__(self,results: dict, profiler : Profiler = None) -> None:
        self.results = results
        if profiler != None :
            self.time_enlapsed = profiler.time_required
            self.memory_used = profiler.max_memory - profiler.min_memory
        else:
            self.time_enlapsed = 'Not profiled'
            self.time_enlapsed = 'Not profiled'

    def set_profiler(self,profiler : Profiler):
        self.time_enlapsed = profiler.time_required
        self.memory_used = profiler.max_memory - profiler.min_memory

    def plot(self,show_sd : bool = False):
        sp = SibillaDataPlotter(self.results)
        sp.show_ensamble_plot(show_sd)
        sp.plot_data()

    def plot_detailed(self):
        sp = SibillaDataPlotter(self.results)
        sp.show_details_plot()
        sp.plot_data()

    def get_results(self):
        return self.results

class SibillaFirstPassageTimeResult:

    def __init__(self,fpt_result : FirstPassageTimeResults):

        self.dict_result ={}

        if fpt_result.getTests()==0:
            self.dict_result['test'] = 0
            return

        if fpt_result.getHits()==0:
            self.dict_result['test'] = fpt_result.getTests()
            self.dict_result['hits'] = 0
            return

        self.dict_result['test'] = fpt_result.getTests()
        self.dict_result['hits'] = fpt_result.getHits()
        self.dict_result['mean'] = fpt_result.getMean()
        self.dict_result['sd'] = fpt_result.getStandardDeviation()
        self.dict_result['min'] = fpt_result.getMin()
        self.dict_result['q1'] = fpt_result.getQ1()
        self.dict_result['q2'] = fpt_result.getQ2()
        self.dict_result['q3'] = fpt_result.getQ3()
        self.dict_result['max'] = fpt_result.getMax()

    def to_dictionary(self):
        return self.dict_result()

    def set_profiler(self,profiler : Profiler):
        self.time_enlapsed = profiler.time_required
        self.memory_used = profiler.max_memory - profiler.min_memory

    def __repr__(self):
        repr_to_ret = ''
        repr_to_ret += f'test : { self.dict_result["test"] } - '

        if self.dict_result['test'] == 0:
            return repr_to_ret

        repr_to_ret += f'hits : { self.dict_result["hits"] } - '

        if self.dict_result['hits'] == 0:
            return repr_to_ret

        repr_to_ret += f'mean : { self.dict_result["mean"] } - '
        repr_to_ret += f'sd : { self.dict_result["sd"] } - '
        repr_to_ret += f'min : { self.dict_result["min"] } - '
        repr_to_ret += f'q1 : { self.dict_result["q1"] } - '
        repr_to_ret += f'q2 : { self.dict_result["q2"] } - '
        repr_to_ret += f'q3 : { self.dict_result["q3"] } - '
        repr_to_ret += f'max : { self.dict_result["max"] } - '

        return repr_to_ret

    def __str__(self):
        str_to_ret = '\n'
        str_to_ret += f'Test : { self.dict_result["test"] } \n '

        if self.dict_result['test'] == 0:
            return str_to_ret

        str_to_ret +=  f'Hits : { self.dict_result["hits"] } \n '

        if self.dict_result['hits'] == 0:
            return str_to_ret

        rounded_mean = round(self.dict_result["mean"], 2)
        str_to_ret += f'Mean : { rounded_mean } \n '

        rounded_sd = round(self.dict_result["sd"], 2)
        str_to_ret += f'SD   : { rounded_sd } \n '

        rounded_min = round(self.dict_result["min"], 2)
        str_to_ret += f'Min  : { rounded_min } \n '

        rounded_q1 = round(self.dict_result["q1"], 2)
        str_to_ret += f'Q1   : { rounded_q1 } \n '

        rounded_q2 = round(self.dict_result["q2"], 2)
        str_to_ret += f'Q2   : { rounded_q2 } \n '

        rounded_q3 = round(self.dict_result["q3"], 2)
        str_to_ret += f'Q3   : { rounded_q3 } \n '

        rounded_max = round(self.dict_result["max"], 2)
        str_to_ret += f'Max  : { rounded_max } \n '

        return str_to_ret

class SibillaReachabilityResult:

    def __init__(self,reach_result , goal: str, delta : float , epsilon : float , condition : str = None) -> None:
        self.result = reach_result
        self.goal = goal
        self.delta = delta
        self.epsilon = epsilon
        self.condition = condition

    def set_profiler(self,profiler : Profiler):
        self.time_enlapsed = profiler.time_required

    def to_float(self):
        return self.result

    def __repr__(self):
        repr_to_ret = ''
        repr_to_ret += f'probability : { self.result } - '
        repr_to_ret += f'goal : { self.goal } - '
        repr_to_ret += f'delta : { self.delta } - '
        repr_to_ret += f'epsilon : { self.epsilon } - '
        if self.condition != None:
            repr_to_ret += f'condition : { self.condition } '
        return repr_to_ret

    def __str__(self) -> str:
        str_to_ret = '\n'

        str_to_ret += f'Probability of reaching {self.goal} is \n'
        str_to_ret += f'{self.result} \n'
        if self.condition != None:
             str_to_ret += f'Fulfilling condition : {self.condition} \n'
        str_to_ret += '\n'
        str_to_ret += f'error prob (epsilon) :  {self.epsilon}\n'
        str_to_ret += f'error gap  (delta)   :  {self.delta}\n'

        return str_to_ret



class JavaToPythonDoubleFunction:
    def __init__(self, java_function):
        self.java_function = java_function

    def __call__(self, arg):
        if isinstance(arg, dict):
            java_map = jnius.autoclass('java.util.HashMap')()
            for k, v in arg.items():
                java_double = jnius.autoclass('java.lang.Double')(float(v))
                java_map.put(k, java_double)
            return self.java_function.applyAsDouble(java_map)
        return self.java_function.applyAsDouble(arg)

class SynthesisRecord:
    def __init__(self, java_synthesis_record):
        self.java_record = java_synthesis_record
        self.time_elapsed = None
        self.memory_used = None

    def set_profiler(self, profiler: Profiler):
        self.time_elapsed = profiler.time_required
        self.memory_used = profiler.max_memory - profiler.min_memory

    def chosen_optimization_algorithm(self) -> str:
        return self.java_record.chosenOptimizationAlgorithm()

    def chosen_surrogate_model(self) -> str:
        return self.java_record.chosenSurrogateModel()

    def chosen_sampling_strategy(self) -> str:
        return self.java_record.chosenSamplingStrategy()

    def objective_function(self) -> Callable[[Dict[str, float]], float]:
        return JavaToPythonDoubleFunction(self.java_record.objectiveFunction())

    def surrogate_function(self) -> Callable[[Dict[str, float]], float]:
        return JavaToPythonDoubleFunction(self.java_record.surrogateFunction())

    def real_fun_dataset(self) -> List[Dict[str, float]]:
        return self.java_record.realFunDataset().toMapList()

    def use_surrogate(self) -> bool:
        return self.java_record.useSurrogate()

    def is_minimization_problem(self) -> bool:
        return self.java_record.isMinimizationProblem()

    def search_space(self) -> Dict[str, tuple]:
        java_search_space = self.java_record.searchSpace()
        return {dim: (java_search_space.getLower(dim), java_search_space.getUpper(dim))
                for dim in java_search_space.getDimensions()}

    def properties(self) -> Dict[str, str]:
        return dict(self.java_record.properties())

    def number_of_samples(self) -> int:
        return self.java_record.numberOfSamples()

    def training_portion(self) -> float:
        return self.java_record.trainingPortion()

    def constraints(self) -> List[Callable[[Dict[str, float]], bool]]:
        return [JavaToPythonDoubleFunction(c) for c in self.java_record.constraints()]

    def optimal_coordinates(self) -> Dict[str, float]:
        java_coords = self.java_record.optimalCoordinates()
        if java_coords is None:
            return None

        result = {}
        entry_set = java_coords.entrySet()
        iterator = entry_set.iterator()
        while iterator.hasNext():
            entry = iterator.next()
            key = entry.getKey()
            value = entry.getValue()
            result[str(key)] = float(value)

        return result

    def optimal_value_objective_function(self) -> float:
        return self.java_record.optimalValueObjectiveFunction()

    def optimal_value_surrogate_function(self) -> float:
        return self.java_record.optimalValueSurrogateFunction()

    def in_sample_metrics(self) -> Dict[str, float]:
        metrics = self.java_record.inSampleMetrics()
        return {'mse': metrics.getMSE(), 'r_squared': metrics.getRSquared()} if metrics else None

    def out_of_sample_metrics(self) -> Dict[str, float]:
        metrics = self.java_record.outOfSampleMetrics()
        return {'mse': metrics.getMSE(), 'r_squared': metrics.getRSquared()} if metrics else None

    def info(self, verbose: bool = False) -> str:
        return self.java_record.info(verbose)

    def set_profiler(self, profiler: Profiler):
        self.time_elapsed = profiler.time_required
        self.memory_used = profiler.max_memory - profiler.min_memory

    def __str__(self) -> str:
        return self._get_string_representation()

    def __repr__(self) -> str:
        return f"SynthesisRecord(java_obj={self.java_record})"

    def _get_string_representation(self, verbose: bool = False) -> str:
        sb = []
        sb.append(f"Optimal Coordinates: {self.optimal_coordinates()}")
        sb.append(f"Optimal Value (Objective Function): {self.optimal_value_objective_function()}")
        sb.append(f"Optimal Value (Surrogate Function): {self.optimal_value_surrogate_function()}")

        if verbose:
            sb.append(f"Optimization Algorithm: {self.chosen_optimization_algorithm()}")
            sb.append(f"Surrogate Model: {self.chosen_surrogate_model()}")
            sb.append(f"Sampling Strategy: {self.chosen_sampling_strategy()}")
            sb.append(f"Use Surrogate: {self.use_surrogate()}")
            sb.append(f"Is Minimization Problem: {self.is_minimization_problem()}")
            sb.append(f"Number of Samples: {self.number_of_samples()}")
            sb.append(f"Training Portion: {self.training_portion()}")
            sb.append(f"Search Space: {self.search_space()}")
            sb.append(f"Properties: {self.properties()}")
            sb.append(f"In-Sample Metrics: {self.in_sample_metrics()}")
            sb.append(f"Out-of-Sample Metrics: {self.out_of_sample_metrics()}")
            sb.append(f"Time Elapsed: {self.time_elapsed}")
            sb.append(f"Memory Used: {self.memory_used}")

        return "\n".join(sb)