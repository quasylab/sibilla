
from memory_profiler import memory_usage
import concurrent.futures
import sys
import time
import jnius

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
      try:
        self.memory_used, self.result = memory_usage(tuple_to_pass,retval=True,interval= 0.001)
      except jnius.JavaException:
        print('ERROR : Something went wrong...')
    
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
    str_to_ret = ''
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
