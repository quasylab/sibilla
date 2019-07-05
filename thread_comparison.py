import matplotlib.pyplot as plt
import pandas

cached_run_data = pandas.read_csv("cached_run_data.data", sep=';', index_col=0)
cached_thread_data = pandas.read_csv("cached_thread_data.data", sep=';', index_col=0)
fixed_run_data = pandas.read_csv("fixed_run_data.data", sep=';', index_col=0)
fixed_thread_data = pandas.read_csv("fixed_thread_data.data", sep=';', index_col=0)



ax=cached_run_data.plot()
plot = fixed_run_data.plot(ax=ax)
plt.gcf().suptitle("Total runtime")
plot.set_ylabel("nanoseconds")
plot.legend(["Cached thread pool", "Fixed thread pool"])
plt.show()
plt.close()

ax = cached_thread_data['average runtime'].plot()
plot = fixed_thread_data['average runtime'].plot(ax=ax)
plt.gcf().suptitle("Average runtime of threads")
plot.set_ylabel("nanoseconds")
plot.legend(["Cached thread pool", "Fixed thread pool"])
plt.show()
plt.close()