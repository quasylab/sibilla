package quasylab.sibilla.core.newserver.master;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import quasylab.sibilla.core.simulator.network.TCPNetworkManagerType;
import quasylab.sibilla.core.simulator.network.UDPNetworkManagerType;

@SpringBootApplication
public class MasterApplication implements CommandLineRunner {

    @Autowired
    MonitoringServerComponent monitoringServerComponent;

    private static final int LOCAL_DISCOVERY_PORT = 10000;
    private static final int REMOTE_DISCOVERY_PORT = 59119;
    private static final UDPNetworkManagerType DISCOVERY_NETWORK_MANAGER_TYPE = UDPNetworkManagerType.DEFAULT;
    private static final int LOCAL_SIMULATION_PORT = 10001;
    private static final TCPNetworkManagerType SIMULATION_NETWORK_MANAGER_TYPE = TCPNetworkManagerType.DEFAULT;
    private static final int LOCAL_MONITORING_PORT = 10002;
    private static final TCPNetworkManagerType MONITORING_NETWORK_MANAGER_TYPE = TCPNetworkManagerType.DEFAULT;

    public static void main(String[] args) {
        SpringApplication.run(MasterApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        MasterServerSimulationEnvironment masterEnvironment = new MasterServerSimulationEnvironment(LOCAL_DISCOVERY_PORT, REMOTE_DISCOVERY_PORT, DISCOVERY_NETWORK_MANAGER_TYPE, LOCAL_SIMULATION_PORT, SIMULATION_NETWORK_MANAGER_TYPE, LOCAL_MONITORING_PORT, MONITORING_NETWORK_MANAGER_TYPE, monitoringServerComponent);
    }
}
