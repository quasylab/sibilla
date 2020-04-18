#!/bin/bash
TYPE=$1
git clone https://github.com/FrancisFire/sibilla.git -b master sibilla_$TYPE
cd sibilla_$TYPE/examples/servers/quasylab.sibilla.examples.servers.$TYPE
gradle build
gradle run
