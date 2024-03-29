# Sibilla
## A tool for forecasting behaviour of Collective Adaptive Systems

[![License](https://img.shields.io/github/license/quasylab/sibilla)](/LICENSE)
[![GitHub contributors](https://img.shields.io/github/contributors/quasylab/sibilla)](https://github.com/quasylab/sibilla/graphs/contributors)
[![GitHub forks](https://img.shields.io/github/forks/quasylab/sibilla?style=social)](https://github.com/quasylab/sibilla/fork)
<hr/>

# Introduction

Sibilla is a Java framework designed to support analysis of Collective Adaptive Systems. These are systems composed by a large set of interactive agents that cooperate and compete to reach local and global goals. 

Sibilla is thought of *container* where different tools supporting specification and analysis of concurrent and distributed large scaled systems can be integrated. Currently Sibilla includes:

* API for *system simulation*;
* API for *transient analysis* via *statistical model checking*;
* API for modelling *population models*.

# Full Documentation
Full documentation of Sibilla can be found in the [Wiki](https://github.com/quasylab/sibilla/wiki) page, with detailed examples and operational details.

# Functionalities

- *Simulation*: The tool permits simulating stochastic processes modelled via different specification languages.
- *Reachability Analysis*: The tool permits estimating the probability that a given set of states (identified by a condition) can be reached within a given amount of time by passing through states satisfying a given predicate.
- *First Passage Time*: The tool permits to estimate the average amount of time needed by a model to reach a given set of states.

# How To Use
To start using Sibilla, follow the guide available in the wiki
