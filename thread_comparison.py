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


ax=cached_run_data.plot()
plot = fixed_run_data.plot(ax=ax)
plt.gcf().suptitle("Total runtime")
plot.set_ylabel("nanoseconds")
plot.legend(["Cached thread pool", "Fixed thread pool"])
plt.show()
plt.close()

ax = cached_thread_data['average runtime'].mean().plot()
plot = fixed_thread_data['average runtime'].mean().plot(ax=ax)
plt.gcf().suptitle("Average runtime of threads")
plot.set_ylabel("nanoseconds")
plot.legend(["Cached thread pool", "Fixed thread pool"])
plt.show()
plt.close()

cached_thread_generated_10k6k["pool size"].plot()
plt.show()
plt.close()


