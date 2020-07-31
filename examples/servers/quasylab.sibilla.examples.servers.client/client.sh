#!/bin/bash
ARGS=$1
if [ "$ARGS" = "-h" ]; then
	echo "Sibilla Client Help:
	-keyStoreType             specify the key store type [JKS]
	-keyStorePath             specify the key store path
	-keyStorePass             specify the key store password
	-trustStoreType           specify the trust store type [JKS]
	-trustStorePath           specify the trust store path
	-trustStorePass           specify the trust store password
	-masterAddress            specify the master server address
	-masterPort               specify the master simulation port
	-masterCommunicationType  specify the type of TCP network communication used for simulations [DEFAULT/SECURE]"

else
	git clone https://github.com/quasylab/sibilla.git -b working sibilla_client
		rm -rf sibilla_client/examples/servers/quasylab.sibilla.examples.servers.master
	rm -rf sibilla_client/examples/servers/quasylab.sibilla.examples.servers.slave
	cd sibilla_client/examples/servers/quasylab.sibilla.examples.servers.client|| exit
	if ["$ARGS" = ""]; then
		gradle run
	else
		gradle run --args="$ARGS"
	fi
fi