import matplotlib.pyplot as plt
import pandas

'''
run_data = pandas.read_csv("thread_data10k6k.data", sep=';', index_col=0)
run_data["pool size"].plot()
plt.show()
plt.close()
'''


cached_run_data = pandas.read_csv("cached_run_data_1000-50.data", sep=';', index_col=0)
cached_thread_data = pandas.read_csv("cached_thread_data_1000-50.data", sep=';', index_col=0).groupby('Concurrent tasks')
fixed_run_data = pandas.read_csv("fixed_run_data_1000-50.data", sep=';', index_col=0)
fixed_thread_data = pandas.read_csv("fixed_thread_data_1000-50.data", sep=';', index_col=0).groupby('Concurrent tasks')
cached_thread_generated_10k6k = pandas.read_csv("thread_data10k6k.data", sep=';', index_col=0)
cached_thread_generated_10k6k_unlimited = pandas.read_csv("thread_data_unlimited_10k6k.data", sep=';')
cached_thread_generated_unlimited = pandas.read_csv("thread_data_unlimited.data", sep=';')
sequential_run_data = pandas.read_csv("run_data_sequential1000.data", sep=";", index_col=0)
test_seq_run_data = pandas.read_csv("test_seq_run_data.data", sep=";", index_col=0)
test_thr_run_data = pandas.read_csv("test_thr_run_data.data", sep=";", index_col=0)

ax=cached_run_data.plot()
plot = fixed_run_data.plot(ax=ax)
plt.gcf().suptitle("Tempo di esecuzione")
plot.set_ylabel("Nanosecondi")
plot.legend(["Cached Thread Pool", "Fixed Thread Pool"])
plt.show()
plt.close()

ax = cached_thread_data['average runtime'].mean().plot()
plot = fixed_thread_data['average runtime'].mean().plot(ax=ax)
plt.gcf().suptitle("Tempo medio d'esecuzione per task")
plot.set_ylabel("nanosecondi")
plot.legend(["Cached Thread Pool", "Fixed Thread Pool"])
plt.show()
plt.close()

ax = cached_thread_generated_10k6k["pool size"].plot()
plot = cached_thread_data["pool size"].mean().plot(ax=ax)
plt.gcf().suptitle("Thread generati Cached Thread Pool con limite")
plot.set_ylabel("Thread")
plot.legend(["10k task con tempo limite 6k", "1k task con tempo limite 600"])
plt.show()
plt.close()

ax = cached_thread_generated_10k6k_unlimited["pool size"].plot()
plot = cached_thread_generated_unlimited["pool size"].plot(ax=ax)
plt.gcf().suptitle("Thread generati Cached Thread Pool senza limite")
plot.set_ylabel("Thread")
plot.legend(["10k task con tempo limite 6k", "1k task con tempo limite 600"])
plt.show()
plt.close()

ax=cached_run_data.plot()
plot = sequential_run_data.plot(ax=ax)
plt.gcf().suptitle("Tempo di esecuzione")
plot.set_ylabel("Nanosecondi")
plot.legend(["Multithread Simulation Manager", "Sequential Simulation Manager"])
plt.show()
plt.close()
