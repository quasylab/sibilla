package quasylab.sibilla.core.master;


import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Component;
import quasylab.sibilla.core.simulator.network.TCPNetworkManagerType;
import quasylab.sibilla.core.simulator.network.UDPNetworkManagerType;
import quasylab.sibilla.core.simulator.newserver.MasterServerSimulationEnvironment;
import quasylab.sibilla.core.simulator.newserver.MonitoringServer;

import javax.management.monitor.Monitor;
import java.io.IOException;

@Component
public class MonitoringServerComponent {
    private static final int LOCAL_DISCOVERY_PORT = 10000;
    private static final int REMOTE_DISCOVERY_PORT = 59119;
    private static final UDPNetworkManagerType DISCOVERY_NETWORK_MANAGER_TYPE = UDPNetworkManagerType.DEFAULT;
    private static final int LOCAL_SIMULATION_PORT = 10001;
    private static final TCPNetworkManagerType SIMULATION_NETWORK_MANAGER_TYPE = TCPNetworkManagerType.DEFAULT;
    private static final int LOCAL_MONITORING_PORT = 10002;
    private static final TCPNetworkManagerType MONITORING_NETWORK_MANAGER_TYPE = TCPNetworkManagerType.DEFAULT;
    private MasterServerSimulationEnvironment masterEnvironment;


    public MonitoringServerComponent() throws IOException, InterruptedException {
        this.masterEnvironment = new MasterServerSimulationEnvironment(LOCAL_DISCOVERY_PORT, REMOTE_DISCOVERY_PORT, DISCOVERY_NETWORK_MANAGER_TYPE, LOCAL_SIMULATION_PORT, SIMULATION_NETWORK_MANAGER_TYPE, LOCAL_MONITORING_PORT, MONITORING_NETWORK_MANAGER_TYPE);
    }

    public MasterServerSimulationEnvironment getMasterEnvironment() {
        return masterEnvironment;
    }
}
