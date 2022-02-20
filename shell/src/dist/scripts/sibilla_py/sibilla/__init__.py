import os
import jnius_config

SSHELL_PATH = os.environ.get('SSHELL_PATH', '')

jnius_config.add_classpath(os.path.join(SSHELL_PATH, 'lib', '*'))

import jnius
import io


################

#   IMPORT TO PLOT   #

from plotly.subplots import make_subplots
import plotly.graph_objects as go
import plotly

################

#   IMPORT TO PROFILING   #

from memory_profiler import memory_usage
import concurrent.futures
import sys
import time


################
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

    def check_loaded_module(self):
        self.__runtime.checkLoadedModule()

    def get_measures(self):
        return self.__runtime.getMeasures()

    def is_enabled_measure(self, name: str):
        return self.__runtime.isEnabledMeasure(name)

    def set_measures(self, *measures: str):
        return self.__runtime.setMeasures(*measures)

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
    
        try:
            if monitor == None:
                results = self.__runtime.simulate(label).to_dict()
                simulation_results = SibillaSimulationResult(results)
                return simulation_results
            else:
                results = self.__runtime.simulate(monitor, label).to_dict()
                simulation_results = SibillaSimulationResult(results)
                return simulation_results
        except jnius.JavaException:
            print("Internal Error") 

    def profiled_simulation(self, label: str, monitor: SimulationMonitor = None):
        profiler = Profiler()
        return profiler.execute(self.simulate,label,monitor)
        
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

    def firstPassageTime(self, predicate_name: str,monitor: SimulationMonitor = None):
        fpt_result = self.__runtime.firstPassageTime(monitor,predicate_name)
        results = {}
        if fpt_result.getTests()==0:
            results['test'] = 0
            return results
        if fpt_result.getHits()==0:
            results['test'] = fpt_result.getTests()
            results['hits'] = 0
            return results
        results['test'] = fpt_result.getTests()
        results['hits'] = fpt_result.getHits()
        results['mean'] = fpt_result.getMean()
        results['sd'] = fpt_result.getStandardDeviation()
        results['min'] = fpt_result.getMin()
        results['q1'] = fpt_result.getQ1()
        results['q2'] = fpt_result.getQ2()
        results['q3'] = fpt_result.getQ3()
        results['max'] = fpt_result.getMax()
        return results

    def compute_prob_reach(self,goal: str, delta:float = 0.01 , epsilon:float = 0.01, monitor: SimulationMonitor=None):
        return self.__runtime.computeProbReach(monitor,goal,delta,epsilon)
    
    def compute_prob_reach_on_condition(self, condition: str, goal: str, delta:float = 0.01 , epsilon:float = 0.01, monitor: SimulationMonitor=None):
        return self.__runtime.computeProbReach(monitor,condition,goal,delta,epsilon)

    @classmethod
    def from_population_model(cls, file_path : str, configuration : str = None, deadline : int = 100, dt : float = 1.0, replica : int = 100):
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

class SibillaDataPlotter():

  def __init__(self, data : dict):
    self.data = data
    self.type_of_measures = 'quantites'
    self.plot_to_show = 'all'
    self.cols = plotly.colors.DEFAULT_PLOTLY_COLORS
    self.data_color_map = self.get_data_color_dict()
    self.plot_template = 'plotly_white'
    if(self.are_we_in_colab()):
        if(self.is_dark_theme_colab()):
            self.plot_template = 'plotly_dark'
    #for ensamble
    self.show_sd = False

  def set_light_theme(self):
    self.plot_template = 'plotly_white'
  
  def set_dark_theme(self):
    self.plot_template = 'plotly_dark'

  def get_data_color_dict(self):
    keys_measures = [(key) for key in self.data if key.startswith('#')]
    color_dict = { }
    color_index = 0
    for key in keys_measures:
      color_dict[key] = self.cols[color_index]
      key = key.replace('#','%')
      color_dict[key] = self.cols[color_index]
      color_index = color_index + 1
    return color_dict

  def select_data_to_plot(self):
    if self.type_of_measures == 'quantites':
      self.key_to_plot = [(key) for key in self.data if key.startswith('#')]
    else:
      self.key_to_plot = [(key) for key in self.data if key.startswith('%')]
  
  def get_subplot_titles(self):
    subplot_titles =[]
    for key in self.key_to_plot:
      newKey = key
      newKey.replace("#","")
      newKey.replace("%","")
      subplot_titles.append(newKey + " with s.d.")
      subplot_titles.append(newKey + " with c.i.")
    return subplot_titles

  def get_total_quantities(self):
    sum = 0
    for key in self.data:
      if key.startswith('#'):
        sum = sum + self.data[key][0][1]
    return sum

  def show_percentage(self):
    self.type_of_measures = 'percentage'
  
  def show_quantities(self):
    self.type_of_measures = 'quantities'
  
  def show_all_plots(self):
    self.plot_to_show = 'all'
  
  def show_ensamble_plot(self,show_sd : bool = False):
    self.plot_to_show = 'ensamble'
    self.show_sd = show_sd
  
  def show_details_plot(self):
    self.plot_to_show = 'details'
  
  def plot_subplots_row(self,subplot_row,key):
    measure_statistics = self.data[key]
    measure_statistics_t = [[measure_statistics[j][i] 
                             for j in range(len(measure_statistics))] 
                            for i in range(len(measure_statistics[0]))]

    current_color =  self.data_color_map[key]
    current_background = current_color
    current_background = current_background.replace('rgb','rgba')
    current_background = current_background.replace(')',', 0.2)')

    time_step = measure_statistics_t[0]
    mean = measure_statistics_t[1]
    sd = measure_statistics_t[2]
    ci = measure_statistics_t[3]
    
    upper_bound_sd = [x + y for x, y in zip(mean, sd)]
    lower_bound_sd = [x - y for x, y in zip(mean, sd)]

    upper_bound_ci = [x + y for x, y in zip(mean, ci)]
    lower_bound_ci = [x - y for x, y in zip(mean, ci)]

    #STANDART DEVIATION

    # mean line
    self.figure_measures_details.add_trace(
        go.Scatter(x=time_step, 
                   y=mean,
                   line=dict(color=current_color),
                   showlegend=False),
              row=subplot_row, 
              col=1)
    #upper sd
    self.figure_measures_details.add_trace(
        go.Scatter(
            x=time_step, 
            y=upper_bound_sd,
            mode='lines',
            marker=dict(color="#444"),
            line=dict(width=0),
            showlegend=False),
        row=subplot_row, 
        col=1)
    #lower sd
    self.figure_measures_details.add_trace(
        go.Scatter(x=time_step, 
                   y=lower_bound_sd,
                   marker=dict(color="#444"), 
                   line=dict(width=0), 
                   mode='lines', 
                   fillcolor=current_background, 
                   fill='tonexty', 
                   showlegend=False),
        row=subplot_row, 
        col=1)
    
    #CONFIENCE INTERVAL

    # mean line
    self.figure_measures_details.add_trace(
        go.Scatter(x=time_step, 
                   y=mean,
                   line=dict(color=current_color),
                   showlegend=False),
              row=subplot_row, 
              col=2)
    #upper ci
    self.figure_measures_details.add_trace(
        go.Scatter(x=time_step, 
                   y=upper_bound_ci,
                   mode='lines',
                   marker=dict(color="#444"),
                   line=dict(width=0),
                   showlegend=False),
              row=subplot_row, 
              col=2)
    #lower ci
    self.figure_measures_details.add_trace(
        go.Scatter(x=time_step, 
                   y=lower_bound_ci,
                   marker=dict(color="#444"), 
                   line=dict(width=0), 
                   mode='lines', 
                   fillcolor=current_background, 
                   fill='tonexty', 
                   showlegend=False),
              row=subplot_row, 
              col=2)
    
  def plot_subplot_ensamble(self,key):
    self.select_data_to_plot()
    current_color =  self.data_color_map[key]
    measure_statistics = self.data[key]
    measure_statistics_t = [[measure_statistics[j][i] for j in range(len(measure_statistics))] for i in range(len(measure_statistics[0]))]
    time_step = measure_statistics_t[0]
    mean = measure_statistics_t[1]
    self.figure_measures_ensamble.add_trace(go.Scatter(x=time_step, 
                                                       y=mean,
                                                       name = key,
                                                       line=dict(color=current_color)),
              row=1, col=1)
  
  def plot_subplot_ensamble_sd(self,key,n):
    self.select_data_to_plot()
    current_color =  self.data_color_map[key]
    current_background = current_color
    current_background = current_background.replace('rgb','rgba')
    current_background = current_background.replace(')',', 0.2)')
    measure_statistics = self.data[key]
    measure_statistics_t = [[measure_statistics[j][i] for j in range(len(measure_statistics))] for i in range(len(measure_statistics[0]))]
    time_step = measure_statistics_t[0]
    mean = measure_statistics_t[1]
    sd = measure_statistics_t[2]

    upper_bound_sd = [x + y for x, y in zip(mean, sd)]
    lower_bound_sd = [x - y for x, y in zip(mean, sd)]
    
    self.figure_measures_ensamble.add_trace(go.Scatter(x=time_step, 
                                                       y=mean,
                                                       legendgroup=str(n),
                                                       showlegend=False,
                                                       name = key,
                                                       line=dict(color=current_color)),
              row=1, col=1)
    self.figure_measures_ensamble.add_trace(
        go.Scatter(
            x=time_step, 
            y=upper_bound_sd,
            legendgroup=str(n),
            mode='lines',
            marker=dict(color="#444"),
            line=dict(width=0),
            showlegend=False),
        row=1, 
        col=1)
    self.figure_measures_ensamble.add_trace(
        go.Scatter(x=time_step, 
                   y=lower_bound_sd,
                   name = key+' with sd',
                   legendgroup=str(n),
                   marker=dict(color="#444"), 
                   line=dict(width=0), 
                   mode='lines', 
                   fillcolor=current_background, 
                   fill='tonexty'),
        row=1, 
        col=1)

  def plot_measures_details(self):
    plot_title = "Measures with standard deviation and confience interval"
    self.select_data_to_plot()
    titles = self.get_subplot_titles()
    row_number = len(self.key_to_plot)
    
    y_axis_max = 1
    if self.type_of_measures == 'quantites':
      y_axis_max = self.get_total_quantities()
    
    self.figure_measures_details = make_subplots(rows=row_number, cols=2,
                    shared_xaxes=True,
                    vertical_spacing=0.1)
    
    self.figure_measures_details.update_yaxes(
        range=[0,y_axis_max], 
        constrain="domain", 
        )
    
    subplot_row = 1

    for key in self.key_to_plot:
      self.plot_subplots_row(subplot_row,key)
      subplot_row = subplot_row + 1
    
    self.figure_measures_details.update_layout(height=1000, 
                                               width=800, 
                                               title_text=plot_title,
                                               template=self.plot_template) 
    self.figure_measures_details.show()

  def plot_ensamble_measures(self):
    plot_title = "Measures ensamble"
    self.select_data_to_plot()
    self.figure_measures_ensamble = make_subplots(rows=1, cols=1,
                         shared_xaxes=True,
                         vertical_spacing=0.1)
    n = 1
    for key in self.key_to_plot:
      if(self.show_sd):
          self.plot_subplot_ensamble_sd(key,n)
          n = n + 1
      else:
          self.plot_subplot_ensamble(key)

    self.figure_measures_ensamble.update_layout(width=800,
                                                title_text=plot_title)
    self.figure_measures_ensamble.update_layout(xaxis=dict(rangeslider=dict(visible=True)),
                                                template=self.plot_template)
    self.figure_measures_ensamble.show()

  def plot_data(self):
    if self.plot_to_show == 'all':
      self.plot_measures_details()
      self.plot_ensamble_measures()
    if self.plot_to_show == 'ensamble':
      self.plot_ensamble_measures()
    if self.plot_to_show == 'details':
      self.plot_measures_details()


class Profiler:

  def __init__(self, running_message : str = 'The function is running', done_message:str = 'Done!') -> None:
      
      """
      :param running_message: a messagge shown during the function execution
      :param done_message: a message shown when the function is terminated
      """

      # Animation instance variables
      self.running_message = running_message
      self.done_message = done_message
      self.animation = self.snake_animation

      # Profilng instance variables 
      self.function_name = None 
      self.time_required = None # second (s)
      self.min_memory = None    # mebibyte (MiB)
      self.max_memory = None    # mebibyte (MiB)
      self.memory_used = None  # [ mebibyte ] (MiB) 

      # where the return value of the function is stored
      self.result = None


  def execute(self, fun_to_profile, *args, **kwargs):
    """
    execute a function passed as a parameter and return the result
    the execution of the function is profiled by keeping track of the 
    total time (in seconds) taken to execute and the memory used (in 
    mebibyte )
    :param fun_to_profile : the function that we want to monitor
    :param *args : parameters of the function
    :param **kwargs : parameters of the function associated with a key value
    
    :return: what would return the function under consideration
    """

    self.function_name = fun_to_profile.__name__

    # check time and memory used to store the function 
    # and store them in the class instance variables
    def execute_profiling():
      tuple_to_pass = (fun_to_profile, args, kwargs)
      t = time.process_time()
      self.memory_used, self.result = memory_usage(tuple_to_pass,retval=True,interval= 0.001)
      self.time_required = time.process_time() - t
      self.min_memory = min(self.memory_used)
      self.max_memory = max(self.memory_used)
      return self.result
    
    # By profiling a certain function it is expected that the 
    # function is not immediate, therefore an animation is shown to
    # give the user feedback on the execution of the function
    with concurrent.futures.ThreadPoolExecutor() as executor:
      future = executor.submit(execute_profiling)
      waiting = True
      n=0
      while(waiting):
        n = self.animation(n)
        waiting = future.running()
      sys.stdout.flush()
      sys.stdout.write('\r '+self.done_message+'\n')
      return_value = future.result()
      return return_value


  def dots_animation(self, n):
    n = n%3+1
    dots = n*'.'+(3-n)*' '
    sys.stdout.write('\r '+self.running_message+' '+ dots)
    sys.stdout.flush()
    time.sleep(0.5)
    return n

  def snake_animation(self, n):
    frames = ["[        ]","[=       ]","[==      ]","[===     ]","[====    ]","[=====   ]",
              "[======  ]","[======= ]","[========]","[ =======]","[  ======]","[   =====]",
              "[    ====]","[     ===]","[      ==]","[       =]","[        ]","[        ]"]
    num_frames = len(frames)
    try:
      n = n%num_frames +1
      sys.stdout.write('\r '+self.running_message+' '+ frames[n])
      sys.stdout.flush()
      time.sleep(0.5)
    except(IndexError):
      sys.stdout.write('\r '+self.running_message+' '+ frames[0])
      sys.stdout.flush()
      time.sleep(0.5)
    return n

  def set_dots_animation(self):
    self.animation = self.dots_animation

  def set_snake_animation(self):
    self.animation = self.snake_animation
  
  def __str__(self):
    str_to_ret = '\n'
    if(self.function_name != None):
      str_to_ret += f'Function name : {self.function_name} \n'
      str_to_ret += f'Time required : {self.time_required} s \n'
      str_to_ret += f'Memory (min)  : {self.min_memory} MiB \n'
      str_to_ret += f'Memory (max)  : {self.max_memory} MiB \n'
      str_to_ret += f'difference    : {self.max_memory-self.min_memory} MiB \n'
    else:
      str_to_ret += 'No functions have been profiled'
    return str_to_ret
    
  def __repr__(self):
    str_to_ret = '\n'
    if(self.function_name != None):
      str_to_ret += f'fun : {self.function_name} --- '
      str_to_ret += f'time : {round(self.time_required, 2)} --- '
      str_to_ret += f'min mem : {round(self.min_memory, 2)} --- '
      str_to_ret += f'max mem : {round(self.max_memory, 2)} --- '
    else:
      str_to_ret += 'No functions have been profiled'
    return str_to_ret

class SibillaSimulationResult():
    def __init__(self,results: dict, profiler : Profiler = None) -> None:
        self.results = results
        if profiler != None :
            self.time_enlapsed = profiler.time_required
            self.memory_used = profiler.max_memory - profiler.min_memory
        else:
            self.time_enlapsed = 'Not profiled'
            self.time_enlapsed = 'Not profiled'

    def plot(self,show_sd : bool = False):
        sp = SibillaDataPlotter(self.results) 
        sp.show_ensamble_plot(show_sd)
        sp.plot_data()

    def get_results(self):
        return self.results