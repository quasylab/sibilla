#!/bin/bash
TYPE=$1
git clone https://github.com/quasylab/sibilla.git -b working sibilla_$TYPE
cd sibilla_$TYPE/examples/servers/quasylab.sibilla.examples.servers.$TYPE
gradle build
gradle run
