import matplotlib.pyplot as plt
import numpy as np

x, y, se = np.loadtxt('data/seir_1000_100.0_FR_.data', delimiter=';', unpack=True)
plt.plot(x,y, label='Loaded from file!')

plt.fill_between(x, y-se, y+se)
plt.xlabel('x')
plt.ylabel('y')
plt.title('Fraction of Infected Agents')
plt.legend()
plt.show()
