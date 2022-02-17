import os
import jnius_config

SSHELL_PATH = os.environ.get('SSHELL_PATH', '')

jnius_config.add_classpath(os.path.join(SSHELL_PATH, 'lib', '*'))

import jnius
import io


#######

#   IMPORT TO PLOT   #

from plotly.subplots import make_subplots
import plotly.graph_objects as go
import plotly

########



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
  
  def show_ensamble_plot(self):
    self.plot_to_show = 'ensamble'
  
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
                   y=lower_bound_ci,
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
    #upper sd
    self.figure_measures_details.add_trace(
        go.Scatter(x=time_step, 
                   y=upper_bound_ci,
                   mode='lines',
                   marker=dict(color="#444"),
                   line=dict(width=0),
                   showlegend=False),
              row=subplot_row, 
              col=2)
    #lower sd
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

  def plot_ensable_measures(self):
    plot_title = "Measures ensamble"
    self.select_data_to_plot()
    self.figure_measures_ensamble = make_subplots(rows=1, cols=1,
                         shared_xaxes=True,
                         vertical_spacing=0.1,
                         subplot_titles=("Plot 1"))
    
    for key in self.key_to_plot:
      self.plot_subplot_ensamble(key)
    self.figure_measures_ensamble.update_layout(width=800,
                                                title_text=plot_title)
    self.figure_measures_ensamble.update_layout(xaxis=dict(rangeslider=dict(visible=True)),
                                                template=self.plot_template)
    self.figure_measures_ensamble.show()

  def plot_data(self):
    if self.plot_to_show == 'all':
      self.plot_measures_details()
      self.plot_ensable_measures()
    if self.plot_to_show == 'ensamble':
      self.plot_ensable_measures()
    if self.plot_to_show == 'details':
      self.plot_measures_details()

class SibillaSimulationResult():
    def __init__(self,results: dict) -> None:
        self.results = results

    def plot(self):
        sp = SibillaDataPlotter(self.results) 
        sp.show_ensamble_plot()
        sp.plot_data()

    def get_results(self):
        return self.results