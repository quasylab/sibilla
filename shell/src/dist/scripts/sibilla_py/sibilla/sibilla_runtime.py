from logging import exception
from .plotting_module import SibillaDataPlotter
from .profiler_module import Profiler
import os
import jnius_config

SSHELL_PATH = os.environ.get('SSHELL_PATH', '')

jnius_config.add_classpath(os.path.join(SSHELL_PATH, 'lib', '*'))

import jnius
import io

SimulationMonitor = jnius.autoclass("it.unicam.quasylab.sibilla.core.simulator.SimulationMonitor")
ShellSimulationMonitor = jnius.autoclass("it.unicam.quasylab.sibilla.shell.ShellSimulationMonitor")
FirstPassageTimeResults = jnius.autoclass("it.unicam.quasylab.sibilla.core.simulator.sampling.FirstPassageTimeResults")
System = jnius.autoclass("java.lang.System")

class Map(jnius.JavaClass, metaclass=jnius.MetaJavaClass):
    __javaclass__ = 'java/util/Map'
    entrySet = jnius.JavaMethod('()Ljava/util/Set;')

    def to_dict(self):
        return {e.getKey():e.getValue() for e in self.entrySet().toArray()}

class SibillaRuntime:

    def __init__(self):
        self.__runtime = jnius.autoclass("it.unicam.quasylab.sibilla.core.runtime.SibillaRuntime")()
        self.current_module = 'NOT SETTE'

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
            try:
                if monitor == None:
                    results = self.__runtime.simulate(label).to_dict()
                else:
                    results = self.__runtime.simulate(monitor, label).to_dict()
            except:
                print('Internal Error')

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

    def evaluate_frist_passage_time(self, predicate_name: str,monitor: SimulationMonitor = None):

        def ftp_runtime( predicate_name: str,monitor: SimulationMonitor = None):
            fpt_result_runtime = self.__runtime.firstPassageTime(monitor,predicate_name)
            fpt_result = SibillaFristPassageTimeResult(fpt_result_runtime)
            return fpt_result

        profiler = Profiler(running_message="Evaluating fpt",done_message="The ftp evaluation has been successfully completed")
        fpt_r = profiler.execute(ftp_runtime, predicate_name, monitor)
        fpt_r.set_profiler(profiler)
        return fpt_r

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
            frist_configuration_found = cls.intial_congiruations[0]
            cls.set_configuration(frist_configuration_found)
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

class SibillaFristPassageTimeResult:

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
