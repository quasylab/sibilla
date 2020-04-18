package quasylab.sibilla.examples.servers.master;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import quasylab.sibilla.core.server.master.MasterServerSimulationEnvironment;
import quasylab.sibilla.core.server.network.TCPNetworkManagerType;
import quasylab.sibilla.core.server.network.UDPNetworkManagerType;
import quasylab.sibilla.core.server.util.SSLUtils;

@SpringBootApplication
public class MasterApplication implements CommandLineRunner {

    private static final int LOCAL_DISCOVERY_PORT = 10000;
    private static final int REMOTE_DISCOVERY_PORT = 59119;
    private static final UDPNetworkManagerType DISCOVERY_NETWORK_MANAGER_TYPE = UDPNetworkManagerType.DEFAULT;
    private static final int LOCAL_SIMULATION_PORT = 10001;
    private static final TCPNetworkManagerType SIMULATION_NETWORK_MANAGER_TYPE = TCPNetworkManagerType.SECURE;
    @Autowired
    MonitoringServerComponent monitoringServerComponent;

    public static void main(String[] args) {
        SpringApplication.run(MasterApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        SSLUtils.getInstance().setKeyStoreType("JKS");
        SSLUtils.getInstance().setKeyStorePath("masterKeyStore.jks");
        SSLUtils.getInstance().setKeyStorePass("masterPass");
        SSLUtils.getInstance().setTrustStoreType("JKS");
        SSLUtils.getInstance().setTrustStorePath("masterTrustStore.jks");
        SSLUtils.getInstance().setTrustStorePass("masterPass");

        MasterServerSimulationEnvironment masterEnvironment = new MasterServerSimulationEnvironment(LOCAL_DISCOVERY_PORT, REMOTE_DISCOVERY_PORT, DISCOVERY_NETWORK_MANAGER_TYPE, LOCAL_SIMULATION_PORT, SIMULATION_NETWORK_MANAGER_TYPE, monitoringServerComponent);
    }
}
