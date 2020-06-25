#!/bin/bash
ARGS=$1
	
if [ "$ARGS" = "-h" ]; then
	echo "Sibilla Master Server Help:
	-keyStoreType                       specify the key store type [JKS]
	-keyStorePath                       specify the key store path
	-keyStorePass                       specify the key store password
	-trustStoreType                     specify the trust store type [JKS]
	-trustStorePath                     specify the trust store path
	-trustStorePass                     specify the trust store password
	-masterDiscoveryPort                specify the local port for discovery
	-slaveDiscoveryPort                 specify the slaves' discovery port
	-masterSimulationPort               specify the local port for simulations
	-slaveDiscoveryCommunicationType    specify the type of UDP network communication used for discovery [DEFAULT]
	-clientSimulationCommunicationType  specify the type of TCP network communication used for simulations [DEFAULT/SECURE]"
	
else
	git clone https://github.com/quasylab/sibilla.git -b working sibilla_master
		rm -rf sibilla_master/examples/servers/quasylab.sibilla.examples.servers.slave
	rm -rf sibilla_master/examples/servers/quasylab.sibilla.examples.servers.client
	cd sibilla_master/examples/servers/quasylab.sibilla.examples.servers.master|| exit
	if ["$ARGS" = ""]; then 
		gradle run
	else
		gradle run --args="$ARGS"
	fi
fi