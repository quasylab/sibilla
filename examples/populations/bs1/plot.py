import matplotlib.pyplot as plt
import numpy as np

x, y, se = np.loadtxt('data/seir_1000_100.0_E_.data', delimiter=';', unpack=True)
plt.plot(x,y, label='Loaded from file!')

plt.fill_between(x, y-e, y+e)
plt.xlabel('x')
plt.ylabel('y')
plt.title('Interesting Graph\nCheck it out')
plt.legend()
plt.show()
