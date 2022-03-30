import matplotlib.pyplot as plt
import csv
import sys

def load_summary_data(datafile):
    time_steps = []
    values = []
    errors = []
    with open(datafile, "r") as f:
        reader = csv.reader(f, delimiter=",")
        for line in reader:
            time_steps.append(float(line[0]))
            values.append(float(line[1]))
            errors.append(float(line[3]))
    return time_steps, values, errors


def plot_summary(time_steps, values, errors, title):
    fig = plt.figure()
    plt.errorbar(time_steps, values, errors)
    plt.title(title)
    plt.show(block=True)
    plt.interactive(True)


if __name__ == "__main__":
    for arg in sys.argv[1:]:
        time_steps, values, errors = load_summary_data(arg)
        plot_summary(time_steps, values, errors, arg)
