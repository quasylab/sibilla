from plotly.subplots import make_subplots
import plotly.graph_objects as go
import plotly


class SibillaDataPlotter:

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

  def are_we_in_colab(self) -> bool:
    try:
      import google.colab
      return True
    except:
      return False

  def is_dark_theme_colab(self) -> bool:
    try:
      from google.colab import output
      return output.eval_js('document.documentElement.matches("[theme=dark]")')
    except:
      return False

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
