#!/bin/bash

SERVER=$1

git clone https://github.com/FrancisFire/sibilla.git -b ssl sibilla_$SERVER
cd sibilla_slave/examples/servers/quasylab.sibilla.examples.servers.$SERVER
gradle build
gradle run
