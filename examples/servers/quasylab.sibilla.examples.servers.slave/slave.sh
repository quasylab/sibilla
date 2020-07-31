#!/bin/bash
ARGS=$1
if [ "$ARGS" = "-h" ]; then
	echo "Sibilla Slave Server Help:
	-keyStoreType	                    	select the key store type [JKS]
	-keyStorePath		                    select the key store path
	-keyStorePass	                    	select the key store password
	-trustStoreType	                   	select the trust store type [JKS]
	-trustStorePath	                  	select the trust store path
	-trustStorePass		                  select the trust store password
	-slaveDiscoveryPort		              specify the local port for discovery
	-slaveSimulationPort	            	specify the local port for simulations
	-masterDiscoveryCommunicationType   specify the type of UDP network communication used for discovery [DEFAULT]
	-masterSimulationCommunicationType	specify the type of TCP network communication used for simulations [DEFAULT/SECURE]"
else
	git clone https://github.com/quasylab/sibilla.git -b working sibilla_slave
	rm -rf sibilla_slave/examples/servers/quasylab.sibilla.examples.servers.master
	rm -rf sibilla_slave/examples/servers/quasylab.sibilla.examples.servers.client
	cd sibilla_slave/examples/servers/quasylab.sibilla.examples.servers.slave|| exit
	if ["$ARGS" = ""]; then
		gradle run
	else
		gradle run --args="$ARGS"
	fi
fi